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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class Mepm extends ProcessflowAbstractTask {

    public static final String HOST_IP = "hostIp";
    public static final String APPLICATION_NAME = "appName";
    private static final Logger LOGGER = LoggerFactory.getLogger(Mepm.class);
    private final DelegateExecution execution;
    private final String action;
    private String appPkgBasePath;
    private RestTemplate restTemplate;
    private String protocol = "https://";
    private static final String URL_PARAM_ERROR = "Failed to resolve url path parameters";

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
            case "configureAppRules":
            case "deleteAppRules":
                configureOrDeleteAppRules(execution);
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
        String applcmIp;
        String applcmPort;
        String appRuleIp;
        String appRulePort;
        String mecmIp = null;
        String mecmPort = null;

        try {
            urlUtil = new UrlUtil();

            applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
            applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);
            appRuleIp = (String) execution.getVariable(Constants.APPRULE_IP);
            appRulePort = (String) execution.getVariable(Constants.APPRULE_PORT);
            String tenant = (String) execution.getVariable(Constants.TENANT_ID);

            if (applcmIp != null && applcmPort != null) {
                urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
                mecmIp = applcmIp;

                urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
                mecmPort = applcmPort;
            }

            if (uri.contains("apprulemgr") && appRuleIp != null && appRulePort != null) {
                urlUtil.addParams(Constants.APPRULE_IP, appRuleIp);
                mecmIp = appRuleIp;
                urlUtil.addParams(Constants.APPRULE_PORT, appRulePort);
                mecmPort = appRulePort;
            }

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
        return protocol + mecmIp + ":" + mecmPort + resolvedUri;
    }

    private void instantiate(DelegateExecution execution) {
        LOGGER.info("Send Instantiate request to applcm");
        String instantiateUrl;
        try {
            instantiateUrl = resolveUrlPathParameters(Constants.APPLCM_INSTANTIATE_URI);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, URL_PARAM_ERROR, Constants.PROCESS_FLOW_ERROR);
            return;
        }

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        String appPackagePath = appPkgBasePath + appInstanceInfo.getAppInstanceId()
                + Constants.SLASH + appInstanceInfo.getAppPackageId() + Constants.APP_PKG_EXT;

        FileSystemResource appPkgRes;
        try {
            appPkgRes = new FileSystemResource(new File(appPackagePath));
        } catch (InvalidPathException e) {
            setProcessflowExceptionResponseAttributes(execution,
                    "Failed to find application package to instantiate", Constants.PROCESS_FLOW_ERROR);
            return;
        }

        // Preparing request parts.
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", appPkgRes);
        parts.add(HOST_IP, appInstanceInfo.getMecHost());
        parts.add(APPLICATION_NAME, appInstanceInfo.getAppName());

        // Preparing HTTP header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        httpHeaders.set(Constants.ACCESS_TOKEN, accessToken);

        try {
            // Creating HTTP entity with header and parts
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

            LOGGER.info("Instantiate application package, host: {}, package: {}, url:{}", appInstanceInfo.getMecHost(),
                    appInstanceInfo.getAppPackageId(),
                    instantiateUrl);
            // Sending request
            ResponseEntity<String> response = restTemplate.exchange(instantiateUrl, HttpMethod.POST,
                    httpEntity, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

                //Delete application package
                String deletePackage = appPkgBasePath + appInstanceInfo.getAppInstanceId()
                        + Constants.SLASH + appInstanceInfo.getAppPackageId() + Constants.APP_PKG_EXT;
                Files.delete(Paths.get(deletePackage));
            } else {
                LOGGER.error(Constants.APPLCM_RETURN_FAILURE, response);
                setProcessflowErrorResponseAttributes(execution,
                        Constants.APPLCM_RETURN_FAILURE, response.getStatusCode().toString());
            }
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_APPLCM, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_APPLCM, Constants.PROCESS_FLOW_ERROR);
        } catch (IOException ex) {
            LOGGER.error("Failed to delete application package {}", appInstanceInfo.getAppPackageId());
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    String.valueOf(ex.getRawStatusCode()));
        }
    }

    private void terminate(DelegateExecution execution) {
        LOGGER.info("Terminate application instance");

        String url;
        try {
            url = resolveUrlPathParameters(Constants.APPLCM_TERMINATE_URI);
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            return;
        }

        ResponseEntity<String> response;
        try {
            HttpHeaders headers = new HttpHeaders();

            String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
            headers.set(Constants.ACCESS_TOKEN, accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Terminate application package, url:{}", url);
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (HttpStatus.OK.equals(response.getStatusCode())) {
                setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
                return;
            }
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, response);
            setProcessflowErrorResponseAttributes(execution,
                    Constants.APPLCM_RETURN_FAILURE, response.getStatusCode().toString());
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_APPLCM, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_APPLCM, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, ex.getResponseBodyAsString());
            if (ex.getResponseBodyAsString().contains("record does not exist in database")
                    || ex.getResponseBodyAsString().contains("not found")
                    || String.valueOf(ex.getRawStatusCode()).equals(Constants.PROCESS_RECORD_NOT_FOUND)) {
                setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
            } else {
                setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                        String.valueOf(ex.getRawStatusCode()));
            }
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
        sendQueryRequestToApplcm(execution, Constants.APPLCM_QUERY_URI);
    }

    private void querykpi(DelegateExecution execution) {
        LOGGER.info("Send query kpi request to applcm");

        sendQueryRequestToApplcm(execution, Constants.APPLCM_QUERY_KPI_URI);
    }

    private void queryEdgeCapabilities(DelegateExecution execution) {
        LOGGER.info("Send query capabilities request to applcm");

        String uri = Constants.APPLCM_QUERY_CAPABILITIES_URI;
        if (null != execution.getVariable(Constants.MEP_CAPABILITY_ID)) {
            uri = Constants.APPLCM_QUERY_CAPABILITY_URI;
        }
        sendQueryRequestToApplcm(execution, uri);
    }

    private void sendQueryRequestToApplcm(DelegateExecution execution, String uri) {
        LOGGER.info("Send query request to applcm");

        String url;
        try {
            url = resolveUrlPathParameters(uri);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            LOGGER.info("Query KPI failed {}", e.getMessage());
            return;
        }

        ResponseEntity<String> response;
        try {
            HttpHeaders headers = new HttpHeaders();

            String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
            headers.set(Constants.ACCESS_TOKEN, accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Query... url:{}", url);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error(Constants.APPLCM_RETURN_FAILURE, response);
                setProcessflowErrorResponseAttributes(execution,
                        Constants.APPLCM_RETURN_FAILURE, response.getStatusCode().toString());
                return;
            }

            String responseStr = response.getBody();
            setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_APPLCM, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_APPLCM, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    String.valueOf(ex.getStatusCode().value()));
        }
    }

    private void configureOrDeleteAppRules(DelegateExecution execution) {
        LOGGER.info("Configure or delete app rules");
        String appRuleUrl;
        try {
            appRuleUrl = resolveUrlPathParameters(Constants.APPRULE_URI);
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, URL_PARAM_ERROR, Constants.PROCESS_FLOW_ERROR);
            return;
        }

        String appRules = (String) execution.getVariable(Constants.UPDATED_APP_RULES);

        // Preparing HTTP header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        httpHeaders.set(Constants.ACCESS_TOKEN, accessToken);

        try {
            // Creating HTTP entity with header
            HttpEntity<String> httpEntity = new HttpEntity<>(appRules, httpHeaders);
            ResponseEntity<String> response;
            switch (action) {
                case "configureAppRules":

                    String appRuleAction = (String) execution.getVariable(Constants.APP_RULE_ACTION);
                    HttpMethod method = HttpMethod.POST;
                    if ("PUT".equals(appRuleAction) || "DELETE".equals(appRuleAction)) {
                        method = HttpMethod.PUT;
                    }

                    LOGGER.info("Configure app rule, method: {}, url: {}, rules: {}", method, appRuleUrl, appRules);
                    // Sending request
                    response = restTemplate.exchange(appRuleUrl, method, httpEntity, String.class);
                    break;
                case "deleteAppRules":
                    LOGGER.info("delete app rule, method: {}, url: {}, rules: {}", HttpMethod.DELETE, appRuleUrl,
                            appRules);
                    // Sending request
                    response = restTemplate.exchange(appRuleUrl, HttpMethod.DELETE, httpEntity, String.class);
                    break;
                default:
                    LOGGER.error("Invalid action {}", action);
                    setProcessflowErrorResponseAttributes(execution, Constants.INTERNAL_ERROR,
                            Constants.PROCESS_FLOW_ERROR);
                    return;
            }

            if (HttpStatus.OK.equals(response.getStatusCode())) {
                setProcessflowResponseAttributes(execution, response.getBody(), Constants.PROCESS_FLOW_SUCCESS);
            } else {
                LOGGER.error(Constants.APPRULE_RETURN_FAILURE, response);
                setProcessflowErrorResponseAttributes(execution, response.getBody(),
                        response.getStatusCode().toString());
            }
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_APPRULE, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_APPRULE, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.APPRULE_RETURN_FAILURE, ex.getResponseBodyAsString());
            if (action.equals("deleteAppRules")
                    && (Constants.PROCESS_FLOW_ERROR_400.equals(String.valueOf(ex.getRawStatusCode()))
                    || Constants.PROCESS_RECORD_NOT_FOUND.equals(String.valueOf(ex.getRawStatusCode())))) {
                setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
            } else {
                setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                        String.valueOf(ex.getRawStatusCode()));
            }
        }
    }
}
