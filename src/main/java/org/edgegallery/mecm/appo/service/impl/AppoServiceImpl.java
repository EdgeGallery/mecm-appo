/*
 * Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.edgegallery.mecm.appo.service.impl;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstantiateReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchCreateParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchInstancesParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchInstancesReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchResponseDto;
import org.edgegallery.mecm.appo.apihandler.dto.BatchTerminateReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.CreateParam;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppRule;
import org.edgegallery.mecm.appo.model.AppRuleTask;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.service.AppoProcessFlowResponse;
import org.edgegallery.mecm.appo.service.AppoProcessflowService;
import org.edgegallery.mecm.appo.service.AppoService;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.edgegallery.mecm.appo.utils.AppoV2Response;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AppoServiceImpl implements AppoService {

    public static final Logger LOGGER = LoggerFactory.getLogger(AppoServiceImpl.class);
    private static final String APP_RULE_PROCESSING = "PROCESSING";
    private static final String REQUEST_ACCEPTED = "Accepted";
    private AppoProcessflowService processflowService;
    private AppInstanceInfoService appInstanceInfoService;

    @Autowired
    public AppoServiceImpl(AppoProcessflowService processflowService, AppInstanceInfoService appInstanceInfoService) {
        this.processflowService = processflowService;
        this.appInstanceInfoService = appInstanceInfoService;
    }

    @Override
    public ResponseEntity<AppoResponse> createAppInstance(String accessToken, String tenantId,
                                                          CreateParam createParam) {
        LOGGER.debug("Application create request received...");

        List<AppInstanceInfo> appInstanceInfos = appInstanceInfoService.getAllAppInstanceInfo(tenantId);
        for (AppInstanceInfo instInfo : appInstanceInfos) {
            if (Constants.OPER_STATUS_INSTANTIATED.equals(instInfo.getOperationalStatus())
                    && createParam.getMecHost().equals(instInfo.getMecHost())
                    && instInfo.getAppName().equals(createParam.getAppName())) {
                LOGGER.error("cannot re-use app name... {}", createParam.getAppName());
                return new ResponseEntity<>(new AppoResponse("cannot re-use app name : " + createParam.getAppName()),
                        HttpStatus.PRECONDITION_FAILED);
            }
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_PACKAGE_ID, createParam.getAppPackageId());
        requestBodyParam.put(Constants.APP_ID, createParam.getAppId());
        requestBodyParam.put(Constants.APP_NAME, createParam.getAppName());
        requestBodyParam.put(Constants.APP_DESCR, createParam.getAppInstanceDescription());
        requestBodyParam.put(Constants.MEC_HOST, createParam.getMecHost());

        String hwCapabilities =
                createParam.getHwCapabilities().stream().map(Object::toString).collect(Collectors.joining(","));
        requestBodyParam.put(Constants.HW_CAPABILITIES, hwCapabilities);

        LOGGER.debug("Create instance input parameters: {}", requestBodyParam);

        String appInstanceID = UUID.randomUUID().toString();
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceID);
        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppInstanceInfo appInstInfo = new AppInstanceInfo();
        appInstInfo.setAppInstanceId(appInstanceID);
        appInstInfo.setAppPackageId(createParam.getAppPackageId());
        appInstInfo.setTenant(tenantId);
        appInstInfo.setAppId(createParam.getAppId());
        appInstInfo.setAppName(createParam.getAppName());
        appInstInfo.setAppDescriptor(createParam.getAppInstanceDescription());
        appInstInfo.setMecHost(createParam.getMecHost());
        appInstInfo.setOperationalStatus(Constants.OPER_STATUS_CREATING);
        appInstanceInfoService.createAppInstanceInfo(tenantId, appInstInfo);

        requestBodyParam.put(Constants.APPRULE_TASK_ID, appInstanceID);
        AppRuleTask appRuleTaskInfo = new AppRuleTask();
        appRuleTaskInfo.setAppRuleTaskId(appInstanceID);
        appRuleTaskInfo.setTenant(tenantId);
        appRuleTaskInfo.setAppInstanceId(appInstanceID);
        appRuleTaskInfo.setConfigResult(APP_RULE_PROCESSING);
        appInstanceInfoService.createAppRuleTaskInfo(tenantId, appRuleTaskInfo);

        processflowService.executeProcessAsync("createApplicationInstance", requestBodyParam);

        Map<String, String> response = new HashMap<>();
        response.put(Constants.APP_INSTANCE_ID, appInstanceID);

        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> createAppInstance(String accessToken, String tenantId,
                                                          BatchCreateParam createParam) {
        LOGGER.debug("Batch application create request received...");

        Map<String, String> batchCreateParam = new HashMap<>();
        batchCreateParam.put(Constants.TENANT_ID, tenantId);
        batchCreateParam.put(Constants.APP_PACKAGE_ID, createParam.getAppPackageId());
        batchCreateParam.put(Constants.APP_ID, createParam.getAppId());
        batchCreateParam.put(Constants.APP_NAME, createParam.getAppName());
        batchCreateParam.put(Constants.APP_DESCR, createParam.getAppInstanceDescription());

        String hosts = createParam.getMecHost().stream().map(Object::toString).collect(Collectors.joining(","));
        batchCreateParam.put(Constants.MEC_HOSTS, hosts);

        String hwCapabilities = createParam.getHwCapabilities().stream().map(Object::toString)
                .collect(Collectors.joining(","));
        batchCreateParam.put(Constants.HW_CAPABILITIES, hwCapabilities);

        LOGGER.debug("Batch create instance input parameters: {}", batchCreateParam);

        List<String> createAppInstanceIds = new LinkedList<>();
        List<BatchResponseDto> response = new LinkedList<>();
        List<AppInstanceInfo> dbAppInstanceInfos = appInstanceInfoService.getAllAppInstanceInfo(tenantId);
        for (String host : createParam.getMecHost()) {
            String appInstanceID = UUID.randomUUID().toString();
            boolean isAppNameReused = false;
            for (AppInstanceInfo instInfo : dbAppInstanceInfos) {
                if (Constants.OPER_STATUS_INSTANTIATED.equals(instInfo.getOperationalStatus())
                        && host.equals(instInfo.getMecHost())
                        && instInfo.getAppName().equals(createParam.getAppName())) {
                    LOGGER.error("cannot re-use app name... {}", createParam.getAppName());
                    BatchResponseDto batchResp = new BatchResponseDto(appInstanceID, host,
                            "cannot re-use app name: " + createParam.getAppName());
                    response.add(batchResp);
                    isAppNameReused = true;
                    break;
                }
            }
            if (isAppNameReused) {
                LOGGER.debug("application name reused, skip create app instance");
                continue;
            }

            createAppInstanceIds.add(appInstanceID);

            BatchResponseDto batchResp = new BatchResponseDto(appInstanceID, host, REQUEST_ACCEPTED);
            response.add(batchResp);

            AppInstanceInfo createAppInstInfo = new AppInstanceInfo();
            createAppInstInfo.setAppInstanceId(appInstanceID);
            createAppInstInfo.setAppPackageId(createParam.getAppPackageId());
            createAppInstInfo.setTenant(tenantId);
            createAppInstInfo.setAppId(createParam.getAppId());
            createAppInstInfo.setAppName(createParam.getAppName());
            createAppInstInfo.setAppDescriptor(createParam.getAppInstanceDescription());
            createAppInstInfo.setMecHost(host);
            createAppInstInfo.setOperationalStatus(Constants.OPER_STATUS_CREATING);
            appInstanceInfoService.createAppInstanceInfo(tenantId, createAppInstInfo);

            batchCreateParam.put(Constants.APPRULE_TASK_ID, appInstanceID);
            AppRuleTask appRuleTask = new AppRuleTask();
            appRuleTask.setAppRuleTaskId(appInstanceID);
            appRuleTask.setTenant(tenantId);
            appRuleTask.setAppInstanceId(appInstanceID);
            appRuleTask.setConfigResult(APP_RULE_PROCESSING);
            appInstanceInfoService.createAppRuleTaskInfo(tenantId, appRuleTask);
        }
        String appInstancesStr = createAppInstanceIds.stream().map(Object::toString)
                .collect(Collectors.joining(","));
        batchCreateParam.put(Constants.APP_INSTANCE_IDS, appInstancesStr);

        batchCreateParam.put(Constants.ACCESS_TOKEN, accessToken);

        processflowService.executeProcessAsync("batchCreateApplicationInstance", batchCreateParam);

        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId,
                                                               String appInstanceId,
                                                               AppInstantiateReqParam instantiationParams) {
        LOGGER.debug("Application instantiation request received...");

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        String operationalStatus = appInstanceInfo.getOperationalStatus();
        if (Constants.OPER_STATUS_INSTANTIATED.equals(operationalStatus)
                || Constants.OPER_STATUS_CREATING.equals(operationalStatus)
                || Constants.OPER_STATUS_CREATE_FAILED.equals(operationalStatus)) {
            LOGGER.error("Application instance operational status is : {}", appInstanceInfo.getOperationalStatus());
            return new ResponseEntity<>(
                    new AppoResponse(
                            Constants.APP_INSTANCE_OPERATIONAL_STATUS + appInstanceInfo.getOperationalStatus()),
                    HttpStatus.PRECONDITION_FAILED);
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        LOGGER.debug("Instantiate input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.APPRULE_TASK_ID, appInstanceId);
        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        if (instantiationParams != null && !instantiationParams.getParameters().isEmpty()) {
            requestBodyParam.put(Constants.INSTANTIATION_PARAMS,
                    new Gson().toJson(instantiationParams.getParameters()));
        }

        processflowService.executeProcessAsync("instantiateApplicationInstance", requestBodyParam);

        return new ResponseEntity<>(new AppoResponse(HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId,
                                                               BatchInstancesReqParam batchInstsParam) {
        LOGGER.debug("Batch application instantiation request received...");

        List<BatchResponseDto> response = new LinkedList<>();
        BatchResponseDto instantiateResp;
        List<BatchInstancesParam> instantiationParameters = new LinkedList<>();
        for (BatchInstancesParam batchInstParam : batchInstsParam.getInstantiationParameters()) {
            String appInstanceId =  batchInstParam.getAppInstanceId();
            try {
                AppInstanceInfo instInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
                String operationalStatus = instInfo.getOperationalStatus();

                if (Constants.OPER_STATUS_INSTANTIATED.equals(operationalStatus)
                        || Constants.OPER_STATUS_CREATING.equals(operationalStatus)
                        || Constants.OPER_STATUS_CREATE_FAILED.equals(operationalStatus)) {
                    LOGGER.error("failed, application instance operational status is : {}",
                            instInfo.getOperationalStatus());
                    BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, instInfo.getMecHost(),
                            "Precondition failed, app instance operational state: " + instInfo.getOperationalStatus());
                    response.add(batchResp);
                } else {
                    instantiateResp = new BatchResponseDto(appInstanceId, instInfo.getMecHost(), REQUEST_ACCEPTED);
                    response.add(instantiateResp);
                    instantiationParameters.add(batchInstParam);
                }
            } catch (NoSuchElementException ex) {
                LOGGER.error("app instance id does not exist: {}", appInstanceId);
                instantiateResp = new BatchResponseDto(appInstanceId, null, ex.getMessage());
                response.add(instantiateResp);
            }
        }

        if (!instantiationParameters.isEmpty()) {
            Map<String, String> batchAppInstsParam = new HashMap<>();
            batchAppInstsParam.put(Constants.TENANT_ID, tenantId);

            String batchInstParams = new Gson().toJson(new BatchInstancesReqParam(instantiationParameters));
            batchAppInstsParam.put(Constants.BATCH_INSTANTIATION_PARAMS, batchInstParams);

            LOGGER.debug("Batch instantiate input params: {}", batchAppInstsParam);

            batchAppInstsParam.put(Constants.BATCH_INSTANTIATION_PARAMS, new Gson().toJson(batchInstsParam));
            batchAppInstsParam.put(Constants.ACCESS_TOKEN, accessToken);

            processflowService.executeProcessAsync("batchInstantiateApplicationInstance", batchAppInstsParam);
        }
        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> getAppInstance(String accessToken, String tenantId, String appInstanceId) {
        LOGGER.debug("Query application info request received...");

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        String operationalStatus = appInstanceInfo.getOperationalStatus();
        if (!Constants.OPER_STATUS_INSTANTIATED.equals(operationalStatus)) {
            LOGGER.error("query failed, application instance operational status is : {}",
                    appInstanceInfo.getOperationalStatus());
            return new ResponseEntity<>(
                    new AppoResponse(
                            Constants.APP_INSTANCE_OPERATIONAL_STATUS + appInstanceInfo.getOperationalStatus()),
                    HttpStatus.PRECONDITION_FAILED);
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        LOGGER.debug("Query application instance input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppoProcessFlowResponse response =
                processflowService.executeProcessSync("queryApplicationInstance", requestBodyParam);
        LOGGER.debug("Query application info response : {} ", response.getResponse());

        return new ResponseEntity<>(new AppoResponse(response.getResponse()),
            HttpStatus.valueOf(response.getResponseCode()));
    }

    @Override
    public ResponseEntity<AppoResponse> terminateAppInstance(String accessToken, String tenantId,
                                                             BatchTerminateReqParam appInstanceParam) {
        LOGGER.debug("Batch application terminate request received...");

        List<String> terminateAppInstIds = new LinkedList<>();
        List<BatchResponseDto> response = new LinkedList<>();
        for (String appInstanceId : appInstanceParam.getAppInstanceIds()) {
            try {
                AppInstanceInfo instanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
                List<AppInstanceDependency> dependencies = appInstanceInfoService
                        .getDependenciesByDependencyAppInstanceId(tenantId, appInstanceId);
                BatchResponseDto resp;
                if (!dependencies.isEmpty()) {
                    LOGGER.error("terminate failed, application instance depended by others");
                    resp = new BatchResponseDto(appInstanceId, instanceInfo.getMecHost(),
                            "application instance depended by others");
                    response.add(resp);
                } else {
                    resp = new BatchResponseDto(appInstanceId, instanceInfo.getMecHost(),
                            REQUEST_ACCEPTED);
                    response.add(resp);
                    terminateAppInstIds.add(appInstanceId);
                }
            } catch (NoSuchElementException ex) {
                BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, null, ex.getMessage());
                response.add(batchResp);
            }
        }

        Map<String, String> terminateReqParam = new HashMap<>();
        terminateReqParam.put(Constants.TENANT_ID, tenantId);

        String terminateAppInstsStr = terminateAppInstIds.stream().map(Object::toString)
                .collect(Collectors.joining(","));
        terminateReqParam.put(Constants.APP_INSTANCE_IDS, terminateAppInstsStr);

        LOGGER.debug("Batch terminate input params: {}", terminateReqParam);

        terminateReqParam.put(Constants.ACCESS_TOKEN, accessToken);

        processflowService.executeProcessAsync("batchTerminateApplicationInstance", terminateReqParam);

        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> terminateAppInstance(String accessToken, String tenantId,
                                                             String appInstanceId) {
        LOGGER.debug("Terminate application info request received...");

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        if (appInstanceInfo == null) {
            LOGGER.error(Constants.APP_INSTANCE_NOT_FOUND);
            throw new NoSuchElementException(Constants.APP_INSTANCE_NOT_FOUND + appInstanceId);
        }

        List<AppInstanceDependency> dependencies =
                appInstanceInfoService.getDependenciesByDependencyAppInstanceId(tenantId, appInstanceId);
        if (!dependencies.isEmpty()) {
            LOGGER.error("Terminated failed, application instance depended by others");
            throw new AppoException("application instance depended by others");
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        LOGGER.debug("Terminate input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);
        requestBodyParam.put(Constants.APPRULE_TASK_ID, appInstanceId);

        processflowService.executeProcessAsync("terminateApplicationInstance", requestBodyParam);

        return new ResponseEntity<>(new AppoResponse(HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoV2Response> queryKpi(String accessToken, String tenantId, String hostIp) {
        LOGGER.debug("Query KPI request received...");

        return platformInfoKpiQuery("queryKpi", accessToken, tenantId, hostIp, null);
    }

    @Override
    public ResponseEntity<String> queryEdgehostCapabilities(String accessToken, String tenantId, String hostIp,
                                                                  String capabilityId) {
        LOGGER.debug("Query MEP capabilities request received...");

        return platformInfoQuery("queryEdgeCapabilities", accessToken, tenantId, hostIp, capabilityId);
    }

    private ResponseEntity<String> platformInfoQuery(String process, String accessToken, String tenantId,
                                                           String hostIp, String capabilityId) {

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.MEC_HOST, hostIp);
        if (capabilityId != null) {
            requestBodyParam.put(Constants.MEP_CAPABILITY_ID, capabilityId);
        }

        LOGGER.debug("Request input: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppoProcessFlowResponse response = processflowService.executeProcessSync(process, requestBodyParam);
        LOGGER.debug("Query response : {} ", response.getResponse());

        if (response.getResponseCode() == HttpStatus.OK.value()) {
            return new ResponseEntity<>(response.getResponse(), HttpStatus.OK);
        }

        return new ResponseEntity<>(response.getResponse(),
                HttpStatus.valueOf(response.getResponseCode()));
    }

    private ResponseEntity<AppoV2Response> platformInfoKpiQuery(String process, String accessToken, String tenantId,
                                                        String hostIp, String capabilityId) {

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.MEC_HOST, hostIp);
        if (capabilityId != null) {
            requestBodyParam.put(Constants.MEP_CAPABILITY_ID, capabilityId);
        }

        LOGGER.debug("Request input: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppoProcessFlowResponse response = processflowService.executeProcessSync(process, requestBodyParam);
        LOGGER.debug("Query response : {} ", response.getResponse());

        AppoV2Response appoV2Response = new Gson().fromJson(response.getResponse(), AppoV2Response.class);

        if (response.getResponseCode() == HttpStatus.OK.value()) {

            return new ResponseEntity<>(appoV2Response, HttpStatus.OK);
        }

        return new ResponseEntity<>(appoV2Response,
                HttpStatus.valueOf(response.getResponseCode()));
    }

    @Override
    public ResponseEntity<AppoResponse> configureAppRules(String accessToken, String tenantId, String appInstanceId,
                                                          AppRule appRule, String action) {
        LOGGER.debug("Application configuration rule request received... action {}", action);
        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        String operationalStatus = appInstanceInfo.getOperationalStatus();
        if (!Constants.OPER_STATUS_INSTANTIATED.equals(operationalStatus)
                && !Constants.OPER_STATUS_CREATED.equals(operationalStatus)) {
            return new ResponseEntity<>(
                    new AppoResponse("Pre condition failed, application instance operational status"
                            + " is : " + appInstanceInfo.getOperationalStatus()),
                    HttpStatus.PRECONDITION_FAILED);
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        requestBodyParam.put(Constants.APP_RULE_ACTION, action);

        Gson gson = new Gson();
        String appRules = gson.toJson(appRule);
        requestBodyParam.put(Constants.APP_RULES, appRules);

        LOGGER.debug("Application rules instance input parameters: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        String appRuleTaskId = UUID.randomUUID().toString();
        requestBodyParam.put(Constants.APPRULE_TASK_ID, appRuleTaskId);

        AppRuleTask appRuleTaskInfo = new AppRuleTask();
        appRuleTaskInfo.setAppRuleTaskId(appRuleTaskId);
        appRuleTaskInfo.setTenant(tenantId);
        appRuleTaskInfo.setAppInstanceId(appInstanceId);
        appRuleTaskInfo.setConfigResult(APP_RULE_PROCESSING);
        appInstanceInfoService.createAppRuleTaskInfo(tenantId, appRuleTaskInfo);

        processflowService.executeProcessAsync("configureAppRules", requestBodyParam);
        Map<String, String> response = new HashMap<>();
        response.put(Constants.APPRULE_TASK_ID, appRuleTaskId);
        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }
}
