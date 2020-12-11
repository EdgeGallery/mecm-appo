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

package org.edgegallery.mecm.appo.service;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import org.edgegallery.mecm.appo.apihandler.BatchCreateParam;
import org.edgegallery.mecm.appo.apihandler.BatchInstancesParam;
import org.edgegallery.mecm.appo.apihandler.CreateParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchResponseDto;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppRule;
import org.edgegallery.mecm.appo.model.AppRuleTask;
import org.edgegallery.mecm.appo.utils.AppoResponse;
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
            if (instInfo.getOperationalStatus().equals(Constants.OPER_STATUS_INSTANTIATED)
                    && instInfo.getAppName().equals(createParam.getAppName())) {
                LOGGER.error("cannot re-use app name...");
                return new ResponseEntity<>(new AppoResponse("cannot re-use app name"), HttpStatus.PRECONDITION_FAILED);
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
        appRuleTaskInfo.setConfigResult("PROCESSING");
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

        List<AppInstanceInfo> appInstanceInfos = appInstanceInfoService.getAllAppInstanceInfo(tenantId);
        for (AppInstanceInfo instInfo : appInstanceInfos) {
            if (instInfo.getOperationalStatus().equals(Constants.OPER_STATUS_INSTANTIATED)
                    && instInfo.getAppName().equals(createParam.getAppName())) {
                LOGGER.error("cannot re-use app name...");
                return new ResponseEntity<>(new AppoResponse("cannot re-use app name"), HttpStatus.PRECONDITION_FAILED);
            }
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_PACKAGE_ID, createParam.getAppPackageId());
        requestBodyParam.put(Constants.APP_ID, createParam.getAppId());
        requestBodyParam.put(Constants.APP_NAME, createParam.getAppName());
        requestBodyParam.put(Constants.APP_DESCR, createParam.getAppInstanceDescription());

        String hosts = createParam.getMecHost().stream().map(Object::toString)
                .collect(Collectors.joining(","));
        requestBodyParam.put(Constants.MEC_HOSTS, hosts);

        String hwCapabilities = createParam.getHwCapabilities().stream().map(Object::toString)
                .collect(Collectors.joining(","));
        requestBodyParam.put(Constants.HW_CAPABILITIES, hwCapabilities);

        LOGGER.debug("Batch create instance input parameters: {}", requestBodyParam);

        List<String> appInstanceIds = new LinkedList<>();
        List<BatchResponseDto> response = new LinkedList<>();
        for (String host : createParam.getMecHost()) {
            String appInstanceID = UUID.randomUUID().toString();
            appInstanceIds.add(appInstanceID);

            BatchResponseDto batchResp = new BatchResponseDto(appInstanceID, host, "Accepted");
            response.add(batchResp);

            AppInstanceInfo appInstInfo = new AppInstanceInfo();
            appInstInfo.setAppInstanceId(appInstanceID);
            appInstInfo.setAppPackageId(createParam.getAppPackageId());
            appInstInfo.setTenant(tenantId);
            appInstInfo.setAppId(createParam.getAppId());
            appInstInfo.setAppName(createParam.getAppName());
            appInstInfo.setAppDescriptor(createParam.getAppInstanceDescription());
            appInstInfo.setMecHost(host);
            appInstInfo.setOperationalStatus(Constants.OPER_STATUS_CREATING);
            appInstanceInfoService.createAppInstanceInfo(tenantId, appInstInfo);

            requestBodyParam.put(Constants.APPRULE_TASK_ID, appInstanceID);
            AppRuleTask appRuleTaskInfo = new AppRuleTask();
            appRuleTaskInfo.setAppRuleTaskId(appInstanceID);
            appRuleTaskInfo.setTenant(tenantId);
            appRuleTaskInfo.setAppInstanceId(appInstanceID);
            appRuleTaskInfo.setConfigResult("PROCESSING");
            appInstanceInfoService.createAppRuleTaskInfo(tenantId, appRuleTaskInfo);
        }
        String appInstancesStr = appInstanceIds.stream().map(Object::toString)
                .collect(Collectors.joining(","));
        requestBodyParam.put(Constants.APP_INSTANCE_IDS, appInstancesStr);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        processflowService.executeProcessAsync("batchCreateApplicationInstance", requestBodyParam);

        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId,
                                                               String appInstanceId) {
        LOGGER.debug("Application instantiation request received...");

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        String operationalStatus = appInstanceInfo.getOperationalStatus();
        if (Constants.OPER_STATUS_INSTANTIATED.equals(operationalStatus)
                || Constants.OPER_STATUS_CREATING.equals(operationalStatus)
                || Constants.OPER_STATUS_CREATE_FAILED.equals(operationalStatus)) {
            LOGGER.error("Application instance operational status is : {}", appInstanceInfo.getOperationalStatus());
            return new ResponseEntity<>(
                    new AppoResponse(
                            "Application instance operational status is : " + appInstanceInfo.getOperationalStatus()),
                    HttpStatus.PRECONDITION_FAILED);
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        LOGGER.debug("Instantiate input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.APPRULE_TASK_ID, appInstanceId);
        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        processflowService.executeProcessAsync("instantiateApplicationInstance", requestBodyParam);

        return new ResponseEntity<>(new AppoResponse(HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId,
                                                               BatchInstancesParam instantiateParam) {
        LOGGER.debug("Batch application instantiation request received...");

        List<String> appInstanceIds = new LinkedList<>();
        List<BatchResponseDto> response = new LinkedList<>();
        for (String appInstanceId : instantiateParam.getAppInstanceIds()) {
            try {
                AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
                String operationalStatus = appInstanceInfo.getOperationalStatus();
                if (Constants.OPER_STATUS_INSTANTIATED.equals(operationalStatus)
                        || Constants.OPER_STATUS_CREATING.equals(operationalStatus)
                        || Constants.OPER_STATUS_CREATE_FAILED.equals(operationalStatus)) {
                    LOGGER.error("Application instance operational status is : {}",
                            appInstanceInfo.getOperationalStatus());
                    BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, appInstanceInfo.getMecHost(),
                            "Precondition failed, app instance operational state: "
                                    + appInstanceInfo.getOperationalStatus());
                    response.add(batchResp);
                } else {
                    BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, appInstanceInfo.getMecHost(),
                            "Accepted");
                    response.add(batchResp);
                    appInstanceIds.add(appInstanceId);
                }
            } catch (NoSuchElementException ex) {
                BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, null, ex.getMessage());
                response.add(batchResp);
            }
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);

        String appInstancesStr = appInstanceIds.stream().map(Object::toString)
                .collect(Collectors.joining(","));
        requestBodyParam.put(Constants.APP_INSTANCE_IDS, appInstancesStr);

        LOGGER.debug("Batch instantiate input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        processflowService.executeProcessAsync("batchInstantiateApplicationInstance", requestBodyParam);

        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> getAppInstance(String accessToken, String tenantId, String appInstanceId) {
        LOGGER.debug("Query application info request received...");

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        String operationalStatus = appInstanceInfo.getOperationalStatus();
        if (!Constants.OPER_STATUS_INSTANTIATED.equals(operationalStatus)) {
            return new ResponseEntity<>(
                    new AppoResponse(
                            "Application instance operational status is : " + appInstanceInfo.getOperationalStatus()),
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
                                                             BatchInstancesParam appInstanceParam) {
        LOGGER.debug("Batch application terminate request received...");

        List<String> appInstanceIds = new LinkedList<>();
        List<BatchResponseDto> response = new LinkedList<>();
        for (String appInstanceId : appInstanceParam.getAppInstanceIds()) {
            try {
                AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
                // TODO: check dependency
                List<AppInstanceDependency> dependencies = appInstanceInfoService
                        .getDependenciesByDependencyAppInstanceId(tenantId, appInstanceId);
                if (dependencies.size() > 0) {
                    LOGGER.error("application instance depended by others");
                    BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, appInstanceInfo.getMecHost(),
                            "application instance depended by others");
                    response.add(batchResp);
                } else {
                    BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, appInstanceInfo.getMecHost(),
                            "Accepted");
                    response.add(batchResp);
                    appInstanceIds.add(appInstanceId);
                }
            } catch (NoSuchElementException ex) {
                BatchResponseDto batchResp = new BatchResponseDto(appInstanceId, null, ex.getMessage());
                response.add(batchResp);
            }
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);

        String appInstancesStr = appInstanceIds.stream().map(Object::toString)
                .collect(Collectors.joining(","));
        requestBodyParam.put(Constants.APP_INSTANCE_IDS, appInstancesStr);

        LOGGER.debug("Batch terminate input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        processflowService.executeProcessAsync("batchTerminateApplicationInstance", requestBodyParam);

        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppoResponse> terminateAppInstance(String accessToken, String tenantId,
                                                             String appInstanceId) {
        LOGGER.debug("Terminate application info request received...");

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        if (appInstanceInfo == null) {
            LOGGER.debug(Constants.APP_INSTANCE_NOT_FOUND);
            throw new NoSuchElementException(Constants.APP_INSTANCE_NOT_FOUND + appInstanceId);
        }

        // TODO: check dependency
        List<AppInstanceDependency> dependencies =
                appInstanceInfoService.getDependenciesByDependencyAppInstanceId(tenantId, appInstanceId);
        if (dependencies.size() > 0) {
            LOGGER.error("application instance depended by others");
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
    public ResponseEntity<AppoResponse> queryKpi(String accessToken, String tenantId, String hostIp) {
        LOGGER.debug("Query KPI request received...");

        return platformInfoQuery("queryKpi", accessToken, tenantId, hostIp, null);
    }

    @Override
    public ResponseEntity<AppoResponse> queryEdgehostCapabilities(String accessToken, String tenantId, String hostIp,
                                                                  String capabilityId) {
        LOGGER.debug("Query MEP capabilities request received...");

        return platformInfoQuery("queryEdgeCapabilities", accessToken, tenantId, hostIp, capabilityId);
    }

    private ResponseEntity<AppoResponse> platformInfoQuery(String process, String accessToken, String tenantId,
                                                           String hostIp, String capabilityId) {

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.MEC_HOST, hostIp);
        requestBodyParam.put(Constants.MEP_CAPABILITY_ID, capabilityId);
        LOGGER.debug("Request input: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppoProcessFlowResponse response = processflowService.executeProcessSync(process, requestBodyParam);
        LOGGER.debug("Query response : {} ", response.getResponse());

        if (response.getResponseCode() == HttpStatus.OK.value()) {
            return new ResponseEntity<>(new AppoResponse(response.getResponse()), HttpStatus.OK);
        }

        return new ResponseEntity<>(new AppoResponse(response.getResponse()),
                HttpStatus.valueOf(response.getResponseCode()));
    }

    @Override
    public ResponseEntity<AppoResponse> configureAppRules(String accessToken, String tenantId, String appInstanceId,
                                                          AppRule appRule, String action) {
        LOGGER.debug("Add application rule request received... action {}", action);
        return configureAppRule(accessToken, tenantId, appInstanceId, appRule, action);
    }

    private ResponseEntity<AppoResponse> configureAppRule(String accessToken, String tenantId, String appInstanceId,
                                                          AppRule appRule, String action) {
        LOGGER.debug("Configure application rule request received...");

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
        appRuleTaskInfo.setConfigResult("PROCESSING");
        appInstanceInfoService.createAppRuleTaskInfo(tenantId, appRuleTaskInfo);

        processflowService.executeProcessAsync("configureAppRules", requestBodyParam);
        Map<String, String> response = new HashMap<>();
        response.put(Constants.APPRULE_TASK_ID, appRuleTaskId);
        return new ResponseEntity<>(new AppoResponse(response), HttpStatus.ACCEPTED);
    }
}
