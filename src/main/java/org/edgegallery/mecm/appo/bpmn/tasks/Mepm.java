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

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppInstantiateReq;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class Mepm extends ProcessflowAbstractTask {

    public static final String HOST_IP = "hostIp";
    public static final String APPLICATION_NAME = "appName";
    public static final String APP_PACKAGE_ID = "packageId";
    private static final Logger LOGGER = LoggerFactory.getLogger(Mepm.class);
    private static final String URL_PARAM_ERROR = "Failed to resolve url path parameters";
    private static final String DELETE_APP_RULES = "deleteAppRules";
    private static final String CONFIGURE_APP_RULES = "configureAppRules";
    private final DelegateExecution execution;
    private final String action;
    private String appPkgBasePath;
    private RestTemplate restTemplate;
    private String protocol = "https://";

    /**
     * Creates an MEPM instance.
     *
     * @param delegateExecution  delegate execution
     * @param restClientTemplate restclient template
     */
    public Mepm(DelegateExecution delegateExecution, boolean isSslEnabled, String appPkgsBasePath,
                RestTemplate restClientTemplate) {
        execution = delegateExecution;
        if (!isSslEnabled) {
            protocol = "http://";
        }
        restTemplate = restClientTemplate;
        appPkgBasePath = appPkgsBasePath;
        action = (String) delegateExecution.getVariable("action");
    }

    /**
     * Retrieves specified inventory.
     */
    public void execute() {
        switch (action) {
            case "instantiate":
                instantiate(execution);
                break;
            case "query":
                queryAppInstance(execution);
                break;
            case "terminate":
                terminate(execution);
                break;
            case "querykpi":
                querykpi(execution);
                break;
            case "queryEdgeCapabilities":
                queryEdgeCapabilities(execution);
                break;
            case CONFIGURE_APP_RULES:
                configureAppRules(execution);
                break;
            case DELETE_APP_RULES:
                deleteAppRules(execution);
                break;
            default:
                LOGGER.info("Invalid MEPM action...{}", action);
                setProcessflowExceptionResponseAttributes(execution, "Invalid MEPM action",
                        Constants.PROCESS_FLOW_ERROR);
        }
    }

    private String resolveUrlPathParameters(String uri) {
        LOGGER.info("Resolve url path parameters...");
        UrlUtil urlUtil;
        String mepmIp = null;
        String mepmPort = null;

        try {
            urlUtil = new UrlUtil();

            mepmIp = (String) execution.getVariable(Constants.MEPM_IP);
            mepmPort = (String) execution.getVariable(Constants.MEPM_PORT);
            String tenant = (String) execution.getVariable(Constants.TENANT_ID);

            urlUtil.addParams(Constants.TENANT_ID, tenant);

            String hostIp = (String) execution.getVariable(Constants.MEC_HOST);
            if (hostIp != null) {
                urlUtil.addParams(Constants.MEC_HOST, hostIp);
            }

            String capabilityId = (String) execution.getVariable(Constants.MEP_CAPABILITY_ID);
            if (capabilityId != null) {
                urlUtil.addParams(Constants.MEP_CAPABILITY_ID, capabilityId);
            }

            AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
            if (appInstanceInfo != null) {
                urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());
            }
        } catch (IllegalArgumentException e) {
            throw new AppoException(e.getMessage());
        }
        String resolvedUri = urlUtil.getUrl(uri);
        return protocol + mepmIp + ":" + mepmPort + resolvedUri;
    }

    private void instantiate(DelegateExecution execution) {
        LOGGER.info("Send Instantiate request to mepm");

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        if (appInstanceInfo == null) {
            setProcessflowErrorResponseAttributes(execution, "app instance info null", Constants.PROCESS_FLOW_ERROR);
            return;
        }

        try {

            // Preparing request headers.
            Map<String, Object> headers = new HashMap<>();
            headers.put("origin", "MEO");

            AppInstantiateReq appInstReq = new AppInstantiateReq(appInstanceInfo.getMecHost(),
                    appInstanceInfo.getAppPackageId(),
                    appInstanceInfo.getAppName());

            LOGGER.info("hostIp {} and appName {}", appInstanceInfo.getMecHost(), appInstanceInfo.getAppName());

            String instantiateUrl = resolveUrlPathParameters(Constants.APPLCM_INSTANTIATE_URI);
            String response = sendRequest(execution, restTemplate, instantiateUrl,
                                          new Gson().toJson(appInstReq), headers, HttpMethod.POST);
            if (response != null) {
                //Delete application package
                String deletePackage = appPkgBasePath + appInstanceInfo.getAppInstanceId()
                        + Constants.SLASH + appInstanceInfo.getAppPackageId() + Constants.APP_PKG_EXT;
                Files.delete(Paths.get(deletePackage));
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to delete application package {}", appInstanceInfo.getAppPackageId());
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, URL_PARAM_ERROR, Constants.PROCESS_FLOW_ERROR);
        } catch (InvalidPathException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    "Failed to find application package to instantiate", Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void terminate(DelegateExecution execution) {
        LOGGER.info("Terminate application instance");

        try {
            String url = resolveUrlPathParameters(Constants.APPLCM_TERMINATE_URI);

            String response = sendRequest(execution, restTemplate, url, HttpMethod.POST);
            if (response == null) {
                String errResponse = (String) execution.getVariable("ErrResponse");
                if (errResponse != null && (errResponse.contains("record does not exist in database")
                        || errResponse.contains("not found"))) {
                    setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
                }
            }
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            return;
        }

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        try {
            if (appInstanceInfo != null) {
                //Delete application package
                String deletePackage = appPkgBasePath + appInstanceInfo.getAppInstanceId()
                        + Constants.SLASH + appInstanceInfo.getAppPackageId() + Constants.APP_PKG_EXT;
                Path path = Paths.get(deletePackage);
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to delete application package {}", appInstanceInfo.getAppPackageId());
        }
    }

    private void queryAppInstance(DelegateExecution execution) {
        LOGGER.info("Query app instance ");

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        if (appInstanceInfo != null
                && !Constants.OPER_STATUS_INSTANTIATED.equals(appInstanceInfo.getOperationalStatus())) {
            setProcessflowErrorResponseAttributes(execution,
                    "Application instance operational status is: " + appInstanceInfo.getOperationalStatus(),
                    Constants.PROCESS_FLOW_SUCCESS);
            return;
        }
        sendQueryRequestToMepm(execution, Constants.APPLCM_QUERY_URI);
    }

    private void querykpi(DelegateExecution execution) {
        LOGGER.info("Send query kpi request to mepm");

        sendQueryRequestToMepm(execution, Constants.APPLCM_QUERY_KPI_URI);
    }

    private void queryEdgeCapabilities(DelegateExecution execution) {
        LOGGER.info("Send query capabilities request to mepm");

        String uri = Constants.APPLCM_QUERY_CAPABILITIES_URI;
        if (null != execution.getVariable(Constants.MEP_CAPABILITY_ID)) {
            uri = Constants.APPLCM_QUERY_CAPABILITY_URI;
        }
        sendQueryRequestToMepm(execution, uri);
    }

    private void sendQueryRequestToMepm(DelegateExecution execution, String uri) {
        LOGGER.info("Send query request to mepm");
        
        try {
            String url = resolveUrlPathParameters(uri);

            sendRequest(execution, restTemplate, url, HttpMethod.GET);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            LOGGER.info("Query KPI failed {}", e.getMessage());
        }
    }

    private void configureAppRules(DelegateExecution execution) {
        LOGGER.info("Configure app rules");

        try {
            String appRuleUrl = resolveUrlPathParameters(Constants.APPRULE_URI);

            String appRuleAction = (String) execution.getVariable(Constants.APP_RULE_ACTION);
            HttpMethod method = HttpMethod.POST;
            if ("PUT".equals(appRuleAction) || "DELETE".equals(appRuleAction)) {
                method = HttpMethod.PUT;
            }

            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);
            sendRequest(execution, restTemplate, appRuleUrl, appRules, method);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, URL_PARAM_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void deleteAppRules(DelegateExecution execution) {
        LOGGER.info("Delete app rules");

        try {
            String appRuleUrl = resolveUrlPathParameters(Constants.APPRULE_URI);

            String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);
            String response = sendRequest(execution, restTemplate, appRuleUrl, appRules, HttpMethod.DELETE);
            if (response == null) {
                String respCode = (String) execution.getVariable("ResponseCode");
                if (Constants.PROCESS_FLOW_ERROR_400.equals(respCode)
                        || Constants.PROCESS_RECORD_NOT_FOUND.equals(respCode)) {
                    setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
                }
            }
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, URL_PARAM_ERROR, Constants.PROCESS_FLOW_ERROR);
        }
    }
}
