/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.mecm.appo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.mecm.appo.apihandler.CreateParam;
import org.edgegallery.mecm.appo.common.AppoProcessFlowResponse;
import org.edgegallery.mecm.appo.common.Constants;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AppoServiceImpl implements AppoService {

    public static final Logger logger = LoggerFactory.getLogger(AppoServiceImpl.class);

    private AppoProcessflowService processflowService;
    private AppInstanceInfoService appInstanceInfoService;

    @Autowired
    public AppoServiceImpl(AppoProcessflowService processflowService, AppInstanceInfoService appInstanceInfoService) {
        this.processflowService = processflowService;
        this.appInstanceInfoService = appInstanceInfoService;
    }

    @Override
    public ResponseEntity<Map<String, String>> createAppInstance(String accessToken, String tenantId,
                                                                 CreateParam createParam) {
        logger.debug("Application create request received...");

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_PACKAGE_ID, createParam.getAppPackageId());
        requestBodyParam.put(Constants.APPD_ID, createParam.getAppdId());
        requestBodyParam.put(Constants.APP_NAME, createParam.getAppName());
        requestBodyParam.put(Constants.APP_INSTANCE_ID, createParam.getAppInstanceDescription());
        requestBodyParam.put(Constants.MEC_HOST, createParam.getMecHost());

        logger.debug("Create instance input parameters: {}", requestBodyParam);

        //Generate application instantiate ID
        String appInstanceID = UUID.randomUUID().toString();
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceID);
        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        //Async flow
        processflowService.executeProcessAsync("createApplicationInstance", requestBodyParam);

        Map<String, String> response = new HashMap<>();
        response.put(Constants.APP_INSTANCE_ID, appInstanceID);

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<String> instantiateAppInstance(String accessToken, String tenantId, String appInstanceId) {
        logger.debug("Application instantiation request received...");

        AppInstanceInfo appInstanceInfo = null;

        appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        if (appInstanceInfo == null) {
            return new ResponseEntity<>("App instance not found", HttpStatus.BAD_REQUEST);
        }

        if (!appInstanceInfo.getOperationalStatus().equals("Created")) {
            return new ResponseEntity<>("Invalid state", HttpStatus.BAD_REQUEST);
        }

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        logger.debug("Instantiate input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        //Async flow
        processflowService.executeProcessAsync("instantiateApplicationInstance", requestBodyParam);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<String> getAppInstance(String accessToken, String tenantId, String appInstanceId) {
        logger.debug("Query application info request received...");

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        logger.debug("Query application instance input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppoProcessFlowResponse response = processflowService.executeProcessSync("queryApplicationInstance",
                requestBodyParam);
        logger.debug("Query application info response : {} ", response.getResponse());

        if (response.getResponseCode() == HttpStatus.OK.value()) {
            return new ResponseEntity<>(response.getResponse(), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.valueOf(response.getResponseCode()));
    }

    @Override
    public ResponseEntity<List<AppInstanceInfo>> getAllAppInstance(String accessToken, String tenantId) {
        logger.debug("Query all application info request received...");

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        logger.debug("Get all application instance input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public ResponseEntity<String> terminateAppInstance(String accessToken,
                                                       String tenantId,
                                                       String appInstanceId) {
        logger.debug("Terminate application info request received...");

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put(Constants.APP_INSTANCE_ID, appInstanceId);
        logger.debug("Terminate input params: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        processflowService.executeProcessAsync("terminateApplicationInstance", requestBodyParam);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<String> queryKpi(String accessToken,
                                           String tenantId,
                                           String hostIp) {
        logger.debug("Query KPI request received...");

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put("host_ip", hostIp);
        logger.debug("Request input: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppoProcessFlowResponse response = processflowService.executeProcessSync("queryKpi", requestBodyParam);
        logger.debug("Query response : {} ", response.getResponse());

        if (response.getResponseCode() == HttpStatus.OK.value()) {
            return new ResponseEntity<>("", HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.valueOf(response.getResponseCode()));
    }

    @Override
    public ResponseEntity<String> queryEdgehostCapabilities(String accessToken,
                                                            String tenantId,
                                                            String hostIp) {
        logger.debug("Query MEP capabilities request received...");

        Map<String, String> requestBodyParam = new HashMap<>();
        requestBodyParam.put(Constants.TENANT_ID, tenantId);
        requestBodyParam.put("host_ip", hostIp);
        logger.debug("Request input: {}", requestBodyParam);

        requestBodyParam.put(Constants.ACCESS_TOKEN, accessToken);

        AppoProcessFlowResponse response = processflowService.executeProcessSync("queryEdgeCapabilities",
                requestBodyParam);
        logger.debug("Query response : {} ", response.getResponse());
        if (response.getResponseCode() == HttpStatus.OK.value()) {
            return new ResponseEntity<>("", HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.valueOf(response.getResponseCode()));
    }
}
