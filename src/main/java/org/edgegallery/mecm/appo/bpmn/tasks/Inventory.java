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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.bpmn.utils.UrlUtility;
import org.edgegallery.mecm.appo.bpmn.utils.restclient.AppoRestClient;
import org.edgegallery.mecm.appo.common.Constants;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inventory extends ProcessflowAbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Inventory.class);

    private final DelegateExecution delegateExecution;
    private final String table;
    private final String mecHostsUrl;
    private final String applcmUrl;

    /**
     * Constructor for get inventory.
     *
     * @param delegateExecution delegate execution
     * @param isSslEnabled      ssl enabled flog
     * @param endPoint          inventory end point
     */
    public Inventory(DelegateExecution delegateExecution, String isSslEnabled, String endPoint) {
        this.delegateExecution = delegateExecution;
        String protocol = getProtocol(isSslEnabled);
        String baseUrl = protocol + endPoint;
        this.table = (String) delegateExecution.getVariable("inventory");

        this.mecHostsUrl = baseUrl + Constants.INVENTORY_MEC_HOST_URI;
        this.applcmUrl = baseUrl + Constants.INVENTORY_APPLCM_URI;
    }

    /**
     * Retrieves specified inventory.
     */
    public void execute() {
        switch (table) {
            case "mecHost":
                getMecHost(delegateExecution);
                break;
            case "applcm":
                getApplcm(delegateExecution);
                break;
            default:
                LOGGER.info("Invalid inventory action...{}", table);
                setProcessflowExceptionResponseAttributes(delegateExecution, "Invalid inventory action",
                        Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves applcm record from inventory.
     *
     * @param delegateExecution delegate execution
     */
    private void getApplcm(DelegateExecution delegateExecution) {
        LOGGER.info("Query applcm from inventory");

        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
        String applcmIp = (String) delegateExecution.getVariable(Constants.APPLCM_IP);
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient client = new AppoRestClient();
        client.addHeader(Constants.ACCESS_TOKEN, accessToken);

        UrlUtility urlUtil = new UrlUtility();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.APPLCM_IP, applcmIp);
        String url = urlUtil.getUrl(applcmUrl);

        try (CloseableHttpResponse response = client.doGet(url)) {

            JSONObject jsonResponse = getResponse(delegateExecution, response);
            if (jsonResponse == null) {
                LOGGER.info("doGet response processing failed...");
                return;
            }

            String applcmPort = jsonResponse.get("applcmPort").toString();
            if (applcmPort == null || applcmPort.isEmpty()) {
                setProcessflowErrorResponseAttributes(delegateExecution,
                        "applcm port not found", Constants.PROCESS_FLOW_ERROR);
                LOGGER.info("applcm port not found... in response");
                return;
            }

            delegateExecution.setVariable("applcm_port", applcmPort);
            setProcessflowResponseAttributes(delegateExecution, "OK", Constants.PROCESS_FLOW_SUCCESS);
        } catch (IOException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves MEC host record from inventory.
     *
     * @param delegateExecution delegate execution
     */
    private void getMecHost(DelegateExecution delegateExecution) {

        LOGGER.info("Query MEC Host from inventory");

        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
        String mecHost = (String) delegateExecution.getVariable(Constants.MEC_HOST);

        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient client = new AppoRestClient();
        client.addHeader(Constants.ACCESS_TOKEN, accessToken);

        UrlUtility urlUtil = new UrlUtility();
        urlUtil.addParams(Constants.TENANT_ID, tenantId);
        urlUtil.addParams(Constants.MEC_HOST, mecHost);
        String url = urlUtil.getUrl(mecHostsUrl);

        try (CloseableHttpResponse response = client.doGet(url)) {
            if (response == null) {
                LOGGER.info("doGet failed...");
                return;
            }
            JSONObject jsonResponse = getResponse(delegateExecution, response);
            if (jsonResponse == null) {
                LOGGER.info("doGet response processing failed...");
                return;
            }

            String applcmIp = jsonResponse.get("applcmIp").toString();
            if (applcmIp == null || applcmIp.isEmpty()) {
                setProcessflowErrorResponseAttributes(delegateExecution,
                        "applcm ip not found", Constants.PROCESS_FLOW_ERROR);
                LOGGER.info("applcm ip not found... in response");
                return;
            }
            delegateExecution.setVariable("applcm_ip", applcmIp);
            setProcessflowResponseAttributes(delegateExecution, "OK", Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException | IOException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, "Could not get MEC host configuration",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Returns json object for the response.
     *
     * @param delegateExecution delegate execution
     * @param response          response
     * @return response as json object
     */
    private JSONObject getResponse(DelegateExecution delegateExecution,
                                   CloseableHttpResponse response) {
        int statusCode;
        JSONObject jsonResponse = null;

        if (response == null) {
            LOGGER.info("Response is null...");
            setProcessflowErrorResponseAttributes(delegateExecution, "Received null response",
                    Constants.PROCESS_FLOW_ERROR);
            return null;
        }

        try {
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Inventory query failed");
                setProcessflowErrorResponseAttributes(delegateExecution, error, String.valueOf(statusCode));
            } else {
                String responseStr = EntityUtils.toString(response.getEntity());
                jsonResponse = (JSONObject) new JSONParser().parse(responseStr);
                setProcessflowResponseAttributes(delegateExecution, "OK", String.valueOf(statusCode));
            }

        } catch (IOException | ParseException | org.apache.http.ParseException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
        return jsonResponse;
    }

    /**
     * Retrieves error info in the response.
     *
     * @param response response
     * @param error    error string
     * @return error string
     * @throws IOException    exception
     * @throws ParseException parse execption
     */
    public String getErrorInfo(CloseableHttpResponse response, String error)
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
