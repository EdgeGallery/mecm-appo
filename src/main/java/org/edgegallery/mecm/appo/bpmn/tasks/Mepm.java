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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.AppoRestClientService;
import org.edgegallery.mecm.appo.utils.AppoRestClient;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mepm extends ProcessflowAbstractTask {

    public static final String HOST_IP = "hostIp";
    private static final Logger LOGGER = LoggerFactory.getLogger(Mepm.class);
    private final DelegateExecution execution;
    private final String action;
    private final String packagePath;
    private String baseUrl;
    private AppoRestClientService restClientService;

    /**
     * Creates an MEPM instance.
     *
     * @param delegateExecution delegate execution
     * @param path              package path
     */
    public Mepm(DelegateExecution delegateExecution, String path, AppoRestClientService appoRestClientService) {
        execution = delegateExecution;
        packagePath = path;
        restClientService = appoRestClientService;
        baseUrl = "{applcm_ip}:{applcm_port}";
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
                query(execution);
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
        try {
            urlUtil = new UrlUtil();

            String applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
            String applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);
            String tenant = (String) execution.getVariable(Constants.TENANT_ID);

            urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
            urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
            urlUtil.addParams(Constants.TENANT_ID, tenant);

            AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
            if (appInstanceInfo != null) {
                urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());
            }
        } catch (IllegalArgumentException e) {
            throw new AppoException(e.getMessage());
        }
        return urlUtil.getUrl(uri);
    }

    private void instantiate(DelegateExecution execution) {
        LOGGER.info("Send Instantiate request to applcm");
        String url;
        try {
            url = resolveUrlPathParameters(baseUrl + Constants.APPLCM_INSTANTIATE_URI);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            return;
        }

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);

        AppoRestClient appoRestClient = restClientService.getAppoRestClient();

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, appInstanceInfo.getMecHost());
        String appPackagePath = packagePath + appInstanceInfo.getAppInstanceId()
                + Constants.SLASH
                + appInstanceInfo.getAppPackageId() + Constants.APP_PKG_EXT;

        appoRestClient.addFileEntity(appPackagePath);

        sendRequest(appoRestClient, Constants.POST, url);
    }

    private void query(DelegateExecution execution) {
        LOGGER.info("Query app instance ");
        String url;
        try {
            url = resolveUrlPathParameters(baseUrl + Constants.APPLCM_INSTANTIATE_URI);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            LOGGER.info("Query application instance failed {}", e.getMessage());
            return;
        }

        String mecHost = null;
        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        if (appInstanceInfo != null) {
            mecHost = appInstanceInfo.getMecHost();
        }

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        AppoRestClient appoRestClient = restClientService.getAppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, mecHost);

        sendRequest(appoRestClient, Constants.GET, url);
    }

    private void terminate(DelegateExecution execution) {
        LOGGER.info("Terminate application instance");

        String url;
        try {
            url = resolveUrlPathParameters(baseUrl + Constants.APPLCM_INSTANTIATE_URI);
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            return;
        }

        String host = null;
        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);
        if (appInstanceInfo != null) {
            host = appInstanceInfo.getMecHost();
        }

        AppoRestClient appoRestClient = restClientService.getAppoRestClient();

        appoRestClient.addHeader(HOST_IP, host);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);

        int statusCode = sendRequest(appoRestClient, Constants.DELETE, url);
        if (!(statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299)) {
            try {
                if (appInstanceInfo != null) {

                    String appPackagePath = packagePath + appInstanceInfo.getAppInstanceId()
                            + Constants.SLASH + appInstanceInfo.getAppPackageId()
                            + Constants.APP_PKG_EXT;
                    Files.delete(Paths.get(appPackagePath));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to delete application package {}", appInstanceInfo.getAppPackageId());
            }
        }
    }

    private void querykpi(DelegateExecution execution) {
        LOGGER.info("Send query kpi request to applcm");

        querykpiOrCapabilities(execution, baseUrl + Constants.APPLCM_QUERY_KPI_URI);
    }

    private void queryEdgeCapabilities(DelegateExecution execution) {
        LOGGER.info("Send query capabilities request to applcm");

        querykpiOrCapabilities(execution, baseUrl + Constants.APPLCM_QUERY_CAPABILITY_URI);
    }

    private void querykpiOrCapabilities(DelegateExecution execution, String uri) {
        LOGGER.info("Send query request to applcm");

        String url;
        try {
            url = resolveUrlPathParameters(uri);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            LOGGER.info("Query KPI failed {}", e.getMessage());
            return;
        }

        AppoRestClient appoRestClient = restClientService.getAppoRestClient();

        String mecHost = (String) execution.getVariable(Constants.MEC_HOST);
        appoRestClient.addHeader(HOST_IP, mecHost);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);

        sendRequest(appoRestClient, Constants.GET, url);
    }

    private int sendRequest(AppoRestClient restClient, String method, String uri) {

        int statusCode = 0;

        try (CloseableHttpResponse response = restClient.sendRequest(method, uri)) {
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                LOGGER.info("Request failed, error code {}", statusCode);
                String error = restClient.getErrorInfo(response, "Request failed, status code {}" + statusCode);
                setProcessflowErrorResponseAttributes(execution, error, String.valueOf(statusCode));
            } else {
                String responseStr = null;
                if (response.getEntity() != null) {
                    responseStr = EntityUtils.toString(response.getEntity());
                    setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
                } else {
                    setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
                }
                LOGGER.info("Response {}, response code {}", responseStr, statusCode);
            }
        } catch (IOException e) {
            LOGGER.info("Request failed due to io exception");

            setProcessflowExceptionResponseAttributes(execution, "Request failed due to io exception",
                    Constants.PROCESS_FLOW_ERROR);
        } catch (ParseException | AppoException e) {
            LOGGER.info("{}", e.getMessage());
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
        return statusCode;
    }
}
