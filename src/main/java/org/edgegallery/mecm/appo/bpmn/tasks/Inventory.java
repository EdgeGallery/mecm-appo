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
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class Inventory extends ProcessflowAbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Inventory.class);
    private static final String STATUS = "status";
    private final DelegateExecution execution;
    private final String table;
    RestTemplate restTemplate;
    private String baseUrl;
    private String protocol = "https://";

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
            case "mepm":
                getMepm(execution);
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
     * Retrieves MEPM record from inventory.
     *
     * @param execution delegate execution
     */
    private void getMepm(DelegateExecution execution) {
        LOGGER.info("Query MEPM from inventory");

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String mepmIp = (String) execution.getVariable(Constants.MEPM_IP);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.MEPM_IP, mepmIp);

        try {
            String mepmUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_MEPM_URI);
            String response = sendRequest(execution, restTemplate, mepmUrl, HttpMethod.GET);
            if (response == null) {
                return;
            }

            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
            JsonElement mepmPort = jsonObject.get("mepmPort");
            if (mepmPort == null) {
                setProcessflowErrorResponseAttributes(execution,
                        "MEPM port not found", Constants.PROCESS_FLOW_ERROR);
                LOGGER.info("MEPM port not found... in response");
                return;
            }
            execution.setVariable(Constants.MEPM_PORT, mepmPort.getAsString());
            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution, Constants.INTERNAL_ERROR,
                    Constants.PROCESS_FLOW_ERROR);
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
            String response = sendRequest(execution, restTemplate, mecUrl, HttpMethod.GET);
            if (response == null) {
                return;
            }
            
            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
            JsonElement mepmIp = jsonObject.get("mepmIp");
            if (mepmIp == null) {
                setProcessflowErrorResponseAttributes(execution,
                        "MEPM IP not configured in host", Constants.PROCESS_FLOW_ERROR);
                LOGGER.info("MEPM IP not configured in host");
                return;
            }
            execution.setVariable(Constants.MEPM_IP, mepmIp.getAsString());

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

        } catch (IllegalArgumentException ex) {
            LOGGER.error("Illegal argument: {}", ex.getMessage());
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

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.MEC_HOST, mecHost);

        try {
            String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
            String appName = (String) execution.getVariable(Constants.APP_NAME);
            String appPkgId = (String) execution.getVariable(Constants.APP_PACKAGE_ID);

            JSONObject appData = new JSONObject();
            appData.put("appInstanceId", appInstId);
            appData.put("appName", appName);
            appData.put("packageId", appPkgId);
            String hwCapabilities = (String) execution.getVariable(Constants.HW_CAPABILITIES);
            if (hwCapabilities != null && !hwCapabilities.isEmpty()) {
                List<String> capList = Arrays.asList(hwCapabilities.split(",", -1));
                appData.put("capabilities", capList);
            }
            String status = (String) execution.getVariable(STATUS);
            appData.put(STATUS, status);

            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPLICATIONS_URI);
            sendRequest(execution, restTemplate, appUrl, appData.toString(), HttpMethod.POST);

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
            sendRequest(execution, restTemplate, appUrl, jsonObject.toString(), HttpMethod.PUT);
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

            return sendRequest(execution, restTemplate, appUrl, HttpMethod.GET);
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
            sendRequest(execution, restTemplate, appUrl, HttpMethod.DELETE);

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

        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);

        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);

            LOGGER.info("Add/Update app rule to Inventory");
            HttpMethod method = HttpMethod.PUT;
            if (getAppRules(execution) == null) {
                method = HttpMethod.POST;
            }
            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);

            sendRequest(execution, restTemplate, appUrl, appRules, method);

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
            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);

            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);

            LOGGER.info("Update app rules to Inventory");
            sendRequest(execution, restTemplate, appUrl, appRules, HttpMethod.PUT);

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

        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

        try {
            String appRuleUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);

            LOGGER.info("Get app rule from Inventory");
            String responseStr = sendRequest(execution, restTemplate, appRuleUrl, HttpMethod.GET);
            if (responseStr != null) {
                execution.setVariable(Constants.INVENTORY_APP_RULES, responseStr);
            }
            return responseStr;
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution, Constants.INTERNAL_ERROR,
                    Constants.PROCESS_FLOW_ERROR);
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
            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);

            String appUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);
            sendRequest(execution, restTemplate, appUrl, appRules, HttpMethod.PUT);

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
    private void deleteAllAppRules(DelegateExecution execution) {

        LOGGER.info("Delete App rules from inventory");
        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String appInstId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);

        try {
            UrlUtil urlUtil = new UrlUtil();
            urlUtil.addParams(Constants.TENANT_ID, tenantId);
            urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstId);

            String appRuleUrl = protocol + baseUrl + urlUtil.getUrl(Constants.INVENTORY_APPRULE_URI);

            sendRequest(execution, restTemplate, appRuleUrl, HttpMethod.DELETE);
        } catch (AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.INTERNAL_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }
}
