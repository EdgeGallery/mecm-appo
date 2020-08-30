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
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.bpmn.utils.UrlUtility;
import org.edgegallery.mecm.appo.bpmn.utils.restclient.AppoRestClient;
import org.edgegallery.mecm.appo.common.Constants;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mepm extends ProcessflowAbstractTask {

    public static final String HOST_IP = "hostIp";
    private static final Logger LOGGER = LoggerFactory.getLogger(Mepm.class);
    private final DelegateExecution delegateExecution;
    private final String action;
    private final String packagePath;
    private final String applcmKpiUrl;
    private final String applcmUrl;
    private final String applcmCapabilitiesUrl;

    /**
     * MEPM Constructor.
     *
     * @param delegateExecution delegate execution
     * @param isSslEnabled      ssl support
     * @param packagePath      package path
     */
    public Mepm(DelegateExecution delegateExecution, String isSslEnabled, String packagePath) {
        this.delegateExecution = delegateExecution;
        this.packagePath = packagePath;
        String protocol = getProtocol(isSslEnabled);

        String urlBase = "{applcm_ip}:{applcm_port}";
        String applcmUrlBase = protocol + urlBase;

        this.applcmUrl = applcmUrlBase + Constants.APPLCM_INSTANTIATE_URI;
        this.applcmKpiUrl = applcmUrlBase + Constants.APPLCM_QUERY_KPI_URI;
        this.applcmCapabilitiesUrl = applcmUrlBase + Constants.APPLCM_QUERY_CAPABILITY_URI;

        this.action = (String) delegateExecution.getVariable("action");
    }

    /**
     * Retrieves specified inventory.
     */
    public void execute() {
        switch (action) {
            case "instantiate":
                instantiate();
                break;
            case "query":
                query();
                break;
            case "terminate":
                terminate();
                break;
            case "querykpi":
                querykpi();
                break;
            case "queryEdgeCapabilities":
                queryEdgeCapabilities();
                break;
            default:
                LOGGER.info("Invalid MEPM action...{}", action);
                setProcessflowExceptionResponseAttributes(delegateExecution, "Invalid MEPM action",
                        Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void instantiate() {
        LOGGER.info("Send Instantiate request to applcm");

        String applcmIp = (String) delegateExecution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) delegateExecution.getVariable(Constants.APPLCM_PORT);

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) delegateExecution.getVariable(Constants.APP_INSTANCE_INFO);

        UrlUtility urlUtil = new UrlUtility();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());

        String url = urlUtil.getUrl(applcmUrl);

        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        AppoRestClient appoRestClient = new AppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, appInstanceInfo.getMecHost());

        HttpEntity entity = appoRestClient.addFileEntity(packagePath + appInstanceInfo.getAppInstanceId()
                + "/" + appInstanceInfo.getAppPackageId());

        try (CloseableHttpResponse response = appoRestClient.doPost(url, entity)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = appoRestClient.getErrorInfo(response, "Instantiate failed");
                setProcessflowErrorResponseAttributes(delegateExecution, error, String.valueOf(statusCode));
            } else {
                setProcessflowResponseAttributes(delegateExecution, "OK", Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException | ParseException | AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void query() {
        LOGGER.info("Query app instance ");

        String applcmIp = (String) delegateExecution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) delegateExecution.getVariable(Constants.APPLCM_PORT);

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) delegateExecution.getVariable("app_instance_info");

        UrlUtility urlUtil = new UrlUtility();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());

        String url = urlUtil.getUrl(applcmUrl);

        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        AppoRestClient appoRestClient = new AppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, appInstanceInfo.getMecHost());

        try (CloseableHttpResponse response = appoRestClient.doGet(url)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Query app instance failed");
                setProcessflowErrorResponseAttributes(delegateExecution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(delegateExecution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException | ParseException | AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            throw new AppoException(e.getMessage());
        }
    }

    private void terminate() {
        LOGGER.info("Terminate application instance");

        String applcmIp = (String) delegateExecution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) delegateExecution.getVariable(Constants.APPLCM_PORT);

        AppInstanceInfo appInstanceInfo = (AppInstanceInfo) delegateExecution.getVariable("app_instance_info");

        UrlUtility urlUtil = new UrlUtility();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);
        urlUtil.addParams(Constants.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());

        String url = urlUtil.getUrl(applcmUrl);

        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        AppoRestClient appoRestClient = new AppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, appInstanceInfo.getMecHost());

        try (CloseableHttpResponse response = appoRestClient.doDelete(url)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Terminate failed");
                setProcessflowErrorResponseAttributes(delegateExecution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(delegateExecution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException | ParseException | AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void querykpi() {
        LOGGER.info("Send query request to applcm");

        String applcmIp = (String) delegateExecution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) delegateExecution.getVariable(Constants.APPLCM_PORT);

        UrlUtility urlUtil = new UrlUtility();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);

        String url = urlUtil.getUrl(applcmKpiUrl);

        String mecHost = (String) delegateExecution.getVariable(Constants.MEC_HOST);
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient appoRestClient = new AppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, mecHost);

        try (CloseableHttpResponse response = appoRestClient.doGet(url)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Query KPI failed");
                setProcessflowErrorResponseAttributes(delegateExecution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(delegateExecution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException | ParseException | AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            throw new AppoException(e.getMessage());
        }
    }

    private void queryEdgeCapabilities() {
        LOGGER.info("Send query request to applcm");

        String applcmIp = (String) delegateExecution.getVariable(Constants.APPLCM_IP);
        String applcmPort = (String) delegateExecution.getVariable(Constants.APPLCM_PORT);

        UrlUtility urlUtil = new UrlUtility();
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        urlUtil.addParams(Constants.APPLCM_PORT, applcmPort);

        String url = urlUtil.getUrl(applcmCapabilitiesUrl);

        String mecHost = (String) delegateExecution.getVariable(Constants.MEC_HOST);
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient appoRestClient = new AppoRestClient();
        appoRestClient.addHeader(Constants.ACCESS_TOKEN, accessToken);
        appoRestClient.addHeader(HOST_IP, mecHost);

        try (CloseableHttpResponse response = appoRestClient.doGet(url)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Query capabilities failed");
                setProcessflowErrorResponseAttributes(delegateExecution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                setProcessflowResponseAttributes(delegateExecution, responseStr, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (IOException | ParseException | AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            throw new AppoException(e.getMessage());
        }
    }

    /**
     * Retrieves error information from response.
     *
     * @param response http response
     * @param error    error string
     * @return error info
     * @throws IOException    io exception
     * @throws ParseException parse exception
     */
    private String getErrorInfo(CloseableHttpResponse response, String error)
            throws IOException, ParseException {
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonResponse = (JSONObject) new JSONParser().parse(responseStr);
        if (jsonResponse.get("error") != null) {
            return jsonResponse.get("error").toString();
        } else {
            return error;
        }
    }
}
