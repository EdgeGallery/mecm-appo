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

package org.edgegallery.mecm.appo.bpmn.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class Inventory extends ProcessflowAbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Inventory.class);

    private final DelegateExecution execution;
    private final String table;
    RestTemplate restTemplate;
    private String baseUrl;
    private String protocol = "https://";
    private static final String STATUS = "status";

    /**
     * Constructor for get inventory.
     *
     * @param delegateExecution delegate execution
     * @param servicePort       inventory end point
     */
    public Inventory(DelegateExecution delegateExecution, boolean isSslEnabled, String servicePort,
                     RestTemplate restClientTemplate) {
        execution = delegateExecution;
        if (!isSslEnabled) {
            protocol = "http://";
        }
        restTemplate = restClientTemplate;
        baseUrl = servicePort;
        table = (String) execution.getVariable("inventory");
    }

    /**
     * Retrieves specified inventory.
     */
    public void execute() {
        switch (table) {
            case "mecHost":
                getMecHost(execution);
                break;
            case "applcm":
                getApplcm(execution);
                break;
            case "appruleCfg":
                getAppRuleCfg(execution);
                break;
            case "application":
                application(execution);
                break;
            case "apprules":
                appRules(execution);
                break;
            default:
                LOGGER.info("Invalid inventory action...{}", table);
                setProcessflowExceptionResponseAttributes(execution, "Invalid inventory",
                        Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves applcm record from inventory.
     *
     * @param execution delegate execution
     */
    private void getApplcm(DelegateExecution execution) {
        LOGGER.info("Query applcm from inventory");

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);

        try {
            String applcmUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPLCM_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Get applcm configuration from Inventory: {}", applcmUrl);
            ResponseEntity<String> response = restTemplate.exchange(applcmUrl, HttpMethod.GET, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get applcm: {} configuration from inventory ", applcmIp);
                setProcessflowErrorResponseAttributes(execution,
                        "failed to get applcm: {} configuration from inventory " + applcmIp,
                        response.getStatusCode().toString());
                return;
            }

            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            JsonElement applcmPort = jsonObject.get("applcmPort");
            if (applcmPort == null) {
                setProcessflowErrorResponseAttributes(execution,
                        "applcm port not found", Constants.PROCESS_FLOW_ERROR);
                LOGGER.info("applcm port not found... in response");
                return;
            }
            execution.setVariable(Constants.APPLCM_PORT, applcmPort.getAsString());
            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution, Constants.INTERNAL_ERROR,
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves applcm record from inventory.
     *
     * @param execution delegate execution
     */
    private void getAppRuleCfg(DelegateExecution execution) {
        LOGGER.info("Query apprule module configuration from inventory");

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);

        String appruleIp = (String) execution.getVariable(Constants.APPRULE_IP);
        urlUtil.addParams(Constants.APPRULE_IP, appruleIp);

        try {
            String appruleUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULECFG_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Get apprule configuration from Inventory: {}", appruleUrl);
            ResponseEntity<String> response = restTemplate.exchange(appruleUrl, HttpMethod.GET, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get apprule: {} configuration from inventory ", appruleIp);
                setProcessflowErrorResponseAttributes(execution,
                        "failed to get apprule: {} configuration from inventory " + appruleIp,
                        response.getStatusCode().toString());
                return;
            }

            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            JsonElement appRulePort = jsonObject.get("appRulePort");
            if (appRulePort == null) {
                setProcessflowErrorResponseAttributes(execution,
                        "apprule port not found", Constants.PROCESS_FLOW_ERROR);
                LOGGER.info("apprule port not found... in response");
                return;
            }
            execution.setVariable(Constants.APPRULE_PORT, appRulePort.getAsString());
            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves MEC host from inventory.
     *
     * @param execution delegate execution
     */
    private void getMecHost(DelegateExecution execution) {

        LOGGER.info("Query MEC Host from inventory");

        try {
            String tenant = (String) execution.getVariable(Constants.TENANT_ID);
            String mecHost = (String) execution.getVariable(Constants.MEC_HOST);
            if (mecHost == null) {
                AppInstanceInfo instanceinfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
                mecHost = instanceinfo.getMecHost();
            }

            UrlUtil urlUtil = new UrlUtil();
            urlUtil.addParams(Constants.TENANT_ID, tenant);
            urlUtil.addParams(Constants.MEC_HOST, mecHost);
            String mecUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_MEC_HOST_URI);

            HttpHeaders headers = new HttpHeaders();
            String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Get MEC Host configuration from Inventory: {}", mecUrl);
            ResponseEntity<String> response = restTemplate.exchange(mecUrl, HttpMethod.GET, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get mec host: {} configuration from inventory ", mecHost);
                setProcessflowErrorResponseAttributes(execution,
                        "failed to get mec host: {} configuration from inventory " + mecHost,
                        response.getStatusCode().toString());
                return;
            }

            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            JsonElement applcmIp = jsonObject.get("applcmIp");
            if (applcmIp == null) {
                setProcessflowErrorResponseAttributes(execution,
                        "applcm IP not found", Constants.PROCESS_FLOW_ERROR);
                LOGGER.info("applcm IP not found... in response");
                return;
            }
            execution.setVariable(Constants.APPLCM_IP, applcmIp.getAsString());

            JsonElement appRuleIp = jsonObject.get("appRuleIp");
            if (appRuleIp == null) {
                LOGGER.info("apprule IP not found... in response");
            } else {
                execution.setVariable(Constants.APPRULE_IP, appRuleIp.getAsString());
            }

            JsonArray hwCapabilities = jsonObject.getAsJsonArray("hwcapabilities");
            List<String> hwTypeList = new LinkedList<>();
            if (hwCapabilities != null) {
                for (int i = 0; i < hwCapabilities.size(); i++) {
                    JsonObject hostCapability = (JsonObject) hwCapabilities.get(i);
                    JsonElement hwType = hostCapability.get("hwType");
                    hwTypeList.add(hwType.getAsString());
                }
                String capabilities = hwTypeList.stream().map(Object::toString)
                        .collect(Collectors.joining(","));
                execution.setVariable("hw_capabilities_list", capabilities);
            }
            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("url: {}", ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    "Failed to resolve url parameters", Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, Constants.INTERNAL_ERROR,
                    Constants.PROCESS_FLOW_ERROR);
        }
    }


    /**
     * Perform CURD operation on application inventory.
     *
     * @param execution delegate execution
     */
    private void application(DelegateExecution execution) {
        LOGGER.info("application inventory");
        String operType = (String) execution.getVariable("operType");
        switch (operType) {
            case "ADD":
                addApplication(execution);
                break;
            case "UPDATE":
                updateApplication(execution);
                break;
            case "GET":
                getApplication(execution);
                break;
            case "DELETE":
                deleteApplication(execution);
                break;
            default:
                LOGGER.info("Invalid inventory operation type...{}", table);
                setProcessflowExceptionResponseAttributes(execution, "Invalid inventory operation type",
                        Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Adds new MEC application to inventory.
     *
     * @param execution delegate execution
     */
    private void addApplication(DelegateExecution execution) {

        LOGGER.info("Add new MEC application to inventory");
        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String mecHost = (String) execution.getVariable(Constants.MEC_HOST);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.MEC_HOST, mecHost);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(Constants.ACCESS_TOKEN, accessToken);

            String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
            String appName = (String) execution.getVariable(Constants.APP_NAME);
            String appPkgId = (String) execution.getVariable(Constants.APP_PACKAGE_ID);

            JSONObject applicationData = new JSONObject();
            applicationData.put("appInstanceId", appInstId);
            applicationData.put("appName", appName);
            applicationData.put("packageId", appPkgId);
            String hwCapabilities = (String) execution.getVariable(Constants.HW_CAPABILITIES);
            if (hwCapabilities != null && !hwCapabilities.isEmpty()) {
                List<String> capList = Arrays.asList(hwCapabilities.split(",", -1));
                applicationData.put("capabilities", capList);
            }
            String status = (String) execution.getVariable(STATUS);
            applicationData.put(STATUS, status);

            HttpEntity<String> entity = new HttpEntity<>(applicationData.toString(), headers);

            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPLICATIONS_URI);
            LOGGER.info("Add application Inventory: {}", appUrl);
            ResponseEntity<String> response = restTemplate.exchange(appUrl, HttpMethod.POST, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to add application inventory record {}", applicationData.toString());
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to add application inventory record " + appInstId,
                        response.getStatusCode().toString());
                return;
            }

            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Updates MEC application to inventory.
     *
     * @param execution delegate execution
     */
    private void updateApplication(DelegateExecution execution) {

        LOGGER.info("Updates MEC application to inventory");
        String applicationjson = getApplication(execution);
        if (applicationjson == null) {
            LOGGER.info("Get Application record failed...");
            return;
        }

        JsonObject jsonObject = new JsonParser().parse(applicationjson).getAsJsonObject();
        JsonElement status = jsonObject.get(STATUS);
        if (status == null) {
            setProcessflowErrorResponseAttributes(execution,
                    "status not found", Constants.PROCESS_FLOW_ERROR);
            LOGGER.info("status not found... in response");
            return;
        }
        String updatestatus = (String) execution.getVariable(STATUS);
        jsonObject.addProperty(STATUS, updatestatus);

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String mecHost = (String) execution.getVariable(Constants.MEC_HOST);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        if (mecHost == null) {
            AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
            if (appInstanceInfo != null) {
                mecHost = appInstanceInfo.getMecHost();
            }
        }
        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.MEC_HOST, mecHost);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPLICATION_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);

            LOGGER.info("Update application to Inventory: {}", appUrl);
            ResponseEntity<String> response = restTemplate.exchange(appUrl, HttpMethod.PUT, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get application: {} from inventory ", appInstId);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to get application from inventory " + appInstId,
                        response.getStatusCode().toString());
                return;
            }
            LOGGER.info("Modified application record: {}", applicationjson);
            String responseStr = response.getBody();
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves MEC application from inventory.
     *
     * @param execution delegate execution
     */
    private String getApplication(DelegateExecution execution) {

        LOGGER.info("Retrieve MEC application from inventory");
        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String mecHost = (String) execution.getVariable(Constants.MEC_HOST);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        if (mecHost == null) {
            AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
            if (appInstanceInfo != null) {
                mecHost = appInstanceInfo.getMecHost();
            }
        }
        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.MEC_HOST, mecHost);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPLICATION_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Get application from Inventory: {}", appUrl);
            ResponseEntity<String> response = restTemplate.exchange(appUrl, HttpMethod.GET, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get application: {} from inventory ", appInstId);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to get application from inventory " + appInstId,
                        response.getStatusCode().toString());
                return null;
            }

            String responseStr = response.getBody();
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            return responseStr;
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_RECORD_NOT_FOUND);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
        return null;
    }

    /**
     * Deletes MEC application from inventory.
     *
     * @param execution delegate execution
     */
    private void deleteApplication(DelegateExecution execution) {

        LOGGER.info("Deletes MEC application from inventory");
        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        if (appInstanceInfo != null) {
            urlUtil.addParams(Constants.MEC_HOST, appInstanceInfo.getMecHost());
        }

        try {
            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPLICATION_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Delete application from Inventory: {}", appUrl);
            ResponseEntity<String> response = restTemplate.exchange(appUrl, HttpMethod.DELETE, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to delete application: {} from inventory ", appInstId);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to delete application from inventory " + appInstId,
                        response.getStatusCode().toString());
                return;
            }

            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Perform CURD operation on application inventory.
     *
     * @param execution delegate execution
     */
    private void appRules(DelegateExecution execution) {
        LOGGER.info("app rules inventory");
        String operType = (String) execution.getVariable("operType");
        switch (operType) {
            case "ADD":
                addAppRules(execution);
                break;
            case "UPDATE":
                updateAppRules(execution);
                break;
            case "GET":
                getAppRules(execution);
                break;
            case "DELETE":
                deleteAppRules(execution);
                break;
            case "DELETEALL":
                deleteAllAppRules(execution);
                break;
            default:
                LOGGER.info("Invalid inventory operation type...{}", table);
                setProcessflowExceptionResponseAttributes(execution, "Invalid inventory operation type",
                        Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Adds new app rule to inventory.
     *
     * @param execution delegate execution
     */
    private void addAppRules(DelegateExecution execution) {

        LOGGER.info("Adding app rules to inventory");

        UrlUtil urlUtil = new UrlUtil();

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        urlUtil.addParams(Constants.TENANT_ID, tenantId);

        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
            headers.set(Constants.ACCESS_TOKEN, accessToken);

            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);
            HttpEntity<String> entity = new HttpEntity<>(appRules, headers);

            LOGGER.info("Add/Update app rule to Inventory: {}", appUrl);
            HttpMethod method = HttpMethod.PUT;
            if (getAppRules(execution) == null) {
                method = HttpMethod.POST;
            }
            ResponseEntity<String> response = restTemplate.exchange(appUrl, method, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to add app rule: {} ", appRules);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to add app rule " + appInstId,
                        response.getStatusCode().toString());
                return;
            }
            LOGGER.info("Added app rule record: {}", appRules);
            String responseStr = response.getBody();
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Updates MEC application to inventory.
     *
     * @param execution delegate execution
     */
    private void updateAppRules(DelegateExecution execution) {

        LOGGER.info("Updates app rules to inventory");

        UrlUtil urlUtil = new UrlUtil();

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
            headers.set(Constants.ACCESS_TOKEN, accessToken);

            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);
            HttpEntity<String> entity = new HttpEntity<>(appRules, headers);

            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);

            LOGGER.info("Update app rules to Inventory: {}", appUrl);
            ResponseEntity<String> response = restTemplate.exchange(appUrl, HttpMethod.PUT, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to update app rule: {} ", appRules);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to update app rule " + appInstId,
                        response.getStatusCode().toString());
                return;
            }
            LOGGER.info("Modified app rule record: {}", appRules);
            String responseStr = response.getBody();
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves App rules from inventory.
     *
     * @param execution delegate execution
     */
    private String getAppRules(DelegateExecution execution) {

        LOGGER.info("Retrieve App rules from inventory");
        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            String appRuleUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Get app rule from Inventory: {}", appRuleUrl);
            ResponseEntity<String> response = restTemplate.exchange(appRuleUrl, HttpMethod.GET, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get app rule: {} from inventory ", appInstId);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to get app rule from inventory " + appInstId,
                        response.getStatusCode().toString());
                return null;
            }

            String responseStr = response.getBody();
            execution.setVariable(Constants.INVENTORY_APP_RULES, responseStr);
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            return responseStr;
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_RECORD_NOT_FOUND);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE,
                    ex.getResponseBodyAsString() + ", error code: " + ex.getRawStatusCode());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    String.valueOf(ex.getRawStatusCode()));
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
        return null;
    }

    /**
     * Deletes MEC application from inventory.
     *
     * @param execution delegate execution
     */
    private void deleteAppRules(DelegateExecution execution) {

        LOGGER.info("Updates app rules to inventory");

        UrlUtil urlUtil = new UrlUtil();

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
            headers.set(Constants.ACCESS_TOKEN, accessToken);

            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);
            HttpEntity<String> entity = new HttpEntity<>(appRules, headers);

            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);
            LOGGER.info("Update application rule to Inventory: {}", appUrl);
            ResponseEntity<String> response = restTemplate.exchange(appUrl, HttpMethod.PUT, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to update app rule: {} ", appRules);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to update app rule " + appInstId,
                        response.getStatusCode().toString());
                return;
            }
            LOGGER.info("Modified app rule record: {}", appRules);
            String responseStr = response.getBody();
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);

        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Deletes all app rules from inventory.
     *
     * @param execution delegate execution
     */
    private String deleteAllAppRules(DelegateExecution execution) {

        LOGGER.info("Delete App rules from inventory");
        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            String appRuleUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.ACCESS_TOKEN, accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Delete app rule from Inventory: {}", appRuleUrl);
            ResponseEntity<String> response = restTemplate.exchange(appRuleUrl, HttpMethod.DELETE, entity,
                    String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to delete app rule: {} from inventory ", appInstId);
                setProcessflowErrorResponseAttributes(execution,
                        "Failed to delete app rule from inventory " + appInstId,
                        response.getStatusCode().toString());
                return null;
            }

            String responseStr = response.getBody();
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            return responseStr;
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_INVENTORY, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_INVENTORY, Constants.PROCESS_RECORD_NOT_FOUND);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.INVENTORY_RETURN_FAILURE, ex.getResponseBodyAsString());
            if (Constants.PROCESS_RECORD_NOT_FOUND.equals(String.valueOf(ex.getRawStatusCode()))) {
                setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
            } else {
                setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                        String.valueOf(ex.getRawStatusCode()));
            }
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
        return null;
    }
}
