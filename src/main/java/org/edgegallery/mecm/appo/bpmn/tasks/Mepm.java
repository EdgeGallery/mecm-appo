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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class Mepm extends ProcessflowAbstractTask {

    public static final String HOST_IP = "hostIp";
    private static final Logger LOGGER = LoggerFactory.getLogger(Mepm.class);
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
        try {
            urlUtil = new UrlUtil();

            applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
            applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);
            String tenant = (String) execution.getVariable(Constants.TENANT_ID);

            urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
            urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
            urlUtil.addParams(Constants.TENANT_ID, tenant);

            String hostIp = (String) execution.getVariable(Constants.MEC_HOST);
            if (hostIp != null) {
                urlUtil.addParams(Constants.MEC_HOST, hostIp);
            }

            AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
            if (appInstanceInfo != null) {
                urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());
            }
        } catch (IllegalArgumentException e) {
            throw new AppoException(e.getMessage());
        }
        String resolvedUri = urlUtil.getUrl(uri);
        return protocol + applcmIp + ":" + applcmPort + resolvedUri;
    }

    private void instantiate(DelegateExecution execution) {
        LOGGER.info("Send Instantiate request to applcm");
        String instantiateUrl;
        try {
            instantiateUrl = resolveUrlPathParameters(Constants.APPLCM_INSTANTIATE_URI);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, "Failed to resolve url path parameters",
                    Constants.PROCESS_FLOW_ERROR);
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
                setProcessflowResponseAttributes(execution, "success", Constants.PROCESS_FLOW_SUCCESS);

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
        } catch (HttpServerErrorException ex) {
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void terminate(DelegateExecution execution) {
        LOGGER.info("Terminate application instance");

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        if (appInstanceInfo != null && !"Instantiated".equals(appInstanceInfo.getOperationalStatus())) {
            setProcessflowResponseAttributes(execution, "", Constants.PROCESS_FLOW_SUCCESS);
            return;
        }

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
                setProcessflowResponseAttributes(execution, "success", Constants.PROCESS_FLOW_SUCCESS);
                return;
            }
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, response);
            setProcessflowErrorResponseAttributes(execution,
                    Constants.APPLCM_RETURN_FAILURE, response.getStatusCode().toString());
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_APPLCM, ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    Constants.FAILED_TO_CONNECT_APPLCM, Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException ex) {
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        }

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
        if (appInstanceInfo != null && !"Instantiated".equals(appInstanceInfo.getOperationalStatus())) {
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

        sendQueryRequestToApplcm(execution, Constants.APPLCM_QUERY_CAPABILITY_URI);
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
        } catch (HttpServerErrorException ex) {
            LOGGER.error(Constants.APPLCM_RETURN_FAILURE, ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    Constants.PROCESS_FLOW_ERROR);
        }
    }
}
