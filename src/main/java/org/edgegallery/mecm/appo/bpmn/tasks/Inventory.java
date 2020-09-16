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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
            headers.set("access_token", accessToken);
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
            setProcessflowExceptionResponseAttributes(execution,
                    "Internal error", Constants.PROCESS_FLOW_ERROR);
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
            headers.set("access_token", accessToken);
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
            setProcessflowExceptionResponseAttributes(execution, "Internal error", Constants.PROCESS_FLOW_ERROR);
        }
    }
}
