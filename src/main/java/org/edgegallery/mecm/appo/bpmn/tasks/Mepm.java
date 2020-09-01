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
import javax.ws.rs.HttpMethod;
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

    private void instantiate(DelegateExecution execution) {
        LOGGER.info("Send Instantiate request to applcm");

        String applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable(Constants.APP_INSTANCE_INFO);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());

        String instantiateUrl = urlUtil.getUrl(baseUrl + Constants.APPLCM_INSTANTIATE_URI);

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient appoRestClient = restClientService.getAppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, appInstanceInfo.getMecHost());
        appoRestClient.addFileEntity(packagePath + appInstanceInfo.getAppInstanceId()
                + "/" + appInstanceInfo.getAppPackageId());

        try (CloseableHttpResponse response = appoRestClient.sendRequest(HttpMethod.POST, instantiateUrl)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = appoRestClient.getErrorInfo(response, "Instantiate failed");
                setProcessflowErrorResponseAttributes(execution, error, String.valueOf(statusCode));
            } else {
                setProcessflowResponseAttributes(execution, "OK", Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException e) {
            LOGGER.info("Instantiate application instance failed io exception");
            setProcessflowExceptionResponseAttributes(execution, "Instantiate application instance failed io exception",
                    Constants.PROCESS_FLOW_ERROR);
        } catch (ParseException | AppoException e) {
            LOGGER.info("{}", e.getMessage());
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(),
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void query(DelegateExecution execution) {
        LOGGER.info("Query app instance ");

        String applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable("app_instance_info");

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());

        String queryUrl = urlUtil.getUrl(baseUrl + Constants.APPLCM_INSTANTIATE_URI);

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        AppoRestClient appoRestClient = restClientService.getAppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, appInstanceInfo.getMecHost());

        try (CloseableHttpResponse response = appoRestClient.sendRequest(HttpMethod.GET, queryUrl)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Query app instance failed");
                setProcessflowErrorResponseAttributes(execution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException e) {
            LOGGER.info("Query application instance failed io exception");
            setProcessflowExceptionResponseAttributes(execution, "Query application instance failed io exception",
                    Constants.PROCESS_FLOW_ERROR);
        } catch (ParseException | AppoException e) {
            LOGGER.info("{}", e.getMessage());
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(),
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void terminate(DelegateExecution execution) {
        LOGGER.info("Terminate application instance");

        String applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) execution.getVariable("app_instance_info");

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());

        String terminateUrl = urlUtil.getUrl(baseUrl + Constants.APPLCM_INSTANTIATE_URI);

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        AppoRestClient appoRestClient = restClientService.getAppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, appInstanceInfo.getMecHost());

        try (CloseableHttpResponse response = appoRestClient.sendRequest(HttpMethod.DELETE, terminateUrl)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Terminate failed");
                setProcessflowErrorResponseAttributes(execution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);

                Files.delete(Paths.get(packagePath + appInstanceInfo.getAppInstanceId()
                        + "/" + appInstanceInfo.getAppPackageId()));
            }
        } catch (IOException e) {
            LOGGER.info("Terminate application instance failed io exception");
            setProcessflowExceptionResponseAttributes(execution, "Terminate application instance failed io exception",
                    Constants.PROCESS_FLOW_ERROR);
        } catch (ParseException | AppoException e) {
            LOGGER.info("{}", e.getMessage());
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(),
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void querykpi(DelegateExecution execution) {
        LOGGER.info("Send query request to applcm");

        String applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);

        String kpiUrl = urlUtil.getUrl(baseUrl + Constants.APPLCM_QUERY_KPI_URI);

        String mecHost = (String) execution.getVariable(Constants.MEC_HOST);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient appoRestClient = restClientService.getAppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, mecHost);

        try (CloseableHttpResponse response = appoRestClient.sendRequest(HttpMethod.GET, kpiUrl)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Query KPI failed");
                setProcessflowErrorResponseAttributes(execution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException e) {
            LOGGER.info("Query kpi failed io exception");
            setProcessflowExceptionResponseAttributes(execution, "Query kpi failed io exception",
                    Constants.PROCESS_FLOW_ERROR);
        } catch (ParseException | AppoException e) {
            LOGGER.info("{}", e.getMessage());
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(),
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void queryEdgeCapabilities(DelegateExecution execution) {
        LOGGER.info("Send query request to applcm");

        String applcmIp = (String) execution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) execution.getVariable(Constants.APPLCM_PORT);

        UrlUtil urlUtil = new UrlUtil();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);

        String capabilityUrl = urlUtil.getUrl(baseUrl + Constants.APPLCM_QUERY_CAPABILITY_URI);

        String mecHost = (String) execution.getVariable(Constants.MEC_HOST);
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient appoRestClient = restClientService.getAppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, mecHost);

        try (CloseableHttpResponse response = appoRestClient.sendRequest(HttpMethod.GET, capabilityUrl)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Query capabilities failed");
                setProcessflowErrorResponseAttributes(execution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(execution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException e) {
            LOGGER.info("Query capabilities failed io exception");
            setProcessflowExceptionResponseAttributes(execution, "Query capabilities failed io exception",
                    Constants.PROCESS_FLOW_ERROR);
        } catch (ParseException | AppoException e) {
            LOGGER.info("{}", e.getMessage());
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(),
                    Constants.PROCESS_FLOW_ERROR);
        }
    }
}
