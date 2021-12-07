/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package org.edgegallery.mecm.appo.bpmn.tasks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public abstract class ProcessflowAbstractTask {

    public static final String RESPONSE = "Response";
    public static final String RESPONSE_CODE = "ResponseCode";
    public static final String ERROR_RESPONSE = "ErrResponse";
    public static final String FLOW_EXCEPTION = "ProcessflowException";
    public static final String ILLEGAL_ARGUMENT = "Illegal Argument...";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessflowAbstractTask.class);

    /**
     * Sets process flow response attributes to delegate execution.
     *
     * @param delegateExecution delegate execution
     * @param response          response
     * @param responseCode      response code
     */
    public void setProcessflowResponseAttributes(DelegateExecution delegateExecution,
                                                 String response, String responseCode) {
        if (responseCode == null) {
            LOGGER.info(ILLEGAL_ARGUMENT);
            throw new IllegalArgumentException();
        }
        LOGGER.info("\nresponse: {} response code: {}\n", response, responseCode);
        delegateExecution.setVariable(RESPONSE, response);
        delegateExecution.setVariable(RESPONSE_CODE, responseCode);
    }

    /**
     * Sets process flow error response attributes to delegate execution.
     *
     * @param delegateExecution delegate execution
     * @param response          response
     * @param responseCode      response code
     */
    public void setProcessflowErrorResponseAttributes(DelegateExecution delegateExecution,
                                                      String response, String responseCode) {
        if (responseCode == null) {
            LOGGER.info(ILLEGAL_ARGUMENT);
            throw new IllegalArgumentException();
        }
        LOGGER.error("\nfailure response: {} response code: {}\n", response, responseCode);
        delegateExecution.setVariable(ERROR_RESPONSE, response);
        delegateExecution.setVariable(RESPONSE_CODE, responseCode);
    }

    /**
     * Sets process flow exception response attributes to delegate execution.
     *
     * @param delegateExecution delegate execution
     * @param response          response
     * @param responseCode      response code
     */
    public void setProcessflowExceptionResponseAttributes(DelegateExecution delegateExecution,
                                                          String response, String responseCode) {
        if (responseCode == null) {
            LOGGER.info(ILLEGAL_ARGUMENT);
            throw new IllegalArgumentException();
        }
        LOGGER.error("\nfailure response: {} response code: {}\n", response, responseCode);
        delegateExecution.setVariable(RESPONSE_CODE, responseCode);
        delegateExecution.setVariable(FLOW_EXCEPTION, response);
        delegateExecution.setVariable(ERROR_RESPONSE, response);
    }

    /**
     * Send request to remote entity.
     * @param execution execution
     * @param restTemplate rest template
     * @param url request url
     * @param data data to send
     * @param method method
     * @return response data
     */
    public String sendRequest(DelegateExecution execution, RestTemplate restTemplate, String url,
                              String data, Map<String, Object> headers, HttpMethod method) {

        HttpHeaders header = getBaseHttpHeader(execution);
        for (Map.Entry<String, Object> entry: headers.entrySet()) {
            header.set(entry.getKey(), entry.getValue().toString());
        }
        HttpEntity<String> entity = new HttpEntity<>(data, header);

        return sendRequest(execution, restTemplate, url, entity, method);

    }

    /**
     * Send request to remote entity.
     * @param execution execution
     * @param restTemplate rest template
     * @param url request url
     * @param method method
     * @return response data
     */
    public String sendRequest(DelegateExecution execution, RestTemplate restTemplate, String url,
                              HttpMethod method) {

        HttpHeaders headers = getBaseHttpHeader(execution);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return sendRequest(execution, restTemplate, url, entity, method);

    }

    /**
     * Send request to remote entity.
     * @param execution execution
     * @param restTemplate rest template
     * @param url request url
     * @param data data to send
     * @param method method
     * @return response data
     */
    public String sendRequest(DelegateExecution execution, RestTemplate restTemplate, String url,
                              String data, HttpMethod method) {

        HttpHeaders headers = getBaseHttpHeader(execution);
        HttpEntity<String> entity = new HttpEntity<>(data, headers);

        return sendRequest(execution, restTemplate, url, entity, method);

    }

    /**
     * Send request to remote entity.
     * @param execution execution
     * @param restTemplate rest template
     * @param url request url
     * @param data data to send
     * @param method method
     * @return response data
     */
    public String sendRequest(DelegateExecution execution, RestTemplate restTemplate, String url,
                              LinkedMultiValueMap<String, Object> data,  MediaType mediaType,
                              HttpMethod method) {

        HttpHeaders headers = getBaseHttpHeader(execution);
        headers.setContentType(mediaType);

        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(data, headers);

        return sendRequestWithMultipartFormData(execution, restTemplate, url, entity, method);

    }

    /**
     * Send request to remote entity.
     * @param execution execution
     * @param restTemplate rest template
     * @param url request url
     * @param entity request entity
     * @param method method
     * @return response data
     */
    public String sendRequest(DelegateExecution execution, RestTemplate restTemplate, String url,
                              HttpEntity<String> entity, HttpMethod method) {

        if (url == null || entity == null || method == null) {
            setProcessflowErrorResponseAttributes(execution, "Invalid input parameters",
                    Constants.PROCESS_FLOW_ERROR);
            return null;
        }

        URI uri = null;
        try {
            uri = new URL(url).toURI();

            LOGGER.info("\n\nSending Request: {}: URL: {}", method, url);

            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.info("Response: \nmethod: {} \nURL: {} \nFailed: {}", method, url, response.getStatusCode());
                setProcessflowErrorResponseAttributes(execution, uri.toString(), response.getStatusCode().toString());
                return null;
            }

            String responseBody = "{}";
            if (response.getBody() != null) {
                responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("data")) {
                    JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
                    if (!jsonObject.get("data").isJsonNull()) {
                        responseBody = jsonObject.get("data").getAsString();
                    } else if (!jsonObject.get("message").isJsonNull()) {
                        responseBody = jsonObject.get("message").getAsString();
                    }
                }
            }
            setProcessflowResponseAttributes(execution, responseBody, Constants.PROCESS_FLOW_SUCCESS);
            return responseBody;
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT + "{}", ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    uri + Constants.FAILED_TO_CONNECT + ex.getMessage(), Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error("failure response from remote entity: {}", ex.getResponseBodyAsString());
            if (HttpMethod.DELETE.equals(method)
                    && Constants.PROCESS_RECORD_NOT_FOUND.equals(String.valueOf(ex.getRawStatusCode()))) {
                setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
            } else {
                setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                        String.valueOf(ex.getRawStatusCode()));
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            setProcessflowExceptionResponseAttributes(execution, ex.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
        return null;
    }

    /**
     * Send request to remote entity.
     * @param execution execution
     * @param restTemplate rest template
     * @param url request url
     * @param entity request entity
     * @param method method
     * @return response data
     */
    public String sendRequestWithMultipartFormData(DelegateExecution execution, RestTemplate restTemplate, String url,
                                       HttpEntity<LinkedMultiValueMap<String, Object>> entity, HttpMethod method) {

        if (url == null || entity == null || method == null) {
            setProcessflowErrorResponseAttributes(execution, "Invalid input parameters",
                    Constants.PROCESS_FLOW_ERROR);
            return null;
        }

        URI uri = null;
        try {
            uri = new URL(url).toURI();

            LOGGER.info("\n\nSending Request: {}: URL: {}", method, url);

            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.info("Response: \nmethod: {} \nURL: {} \nFailed: {}", method, url, response.getStatusCode());
                setProcessflowErrorResponseAttributes(execution, uri.toString(), response.getStatusCode().toString());
                return null;
            }

            String responseBody = "{}";
            if (response.getBody() != null) {
                responseBody = response.getBody();
            }
            setProcessflowResponseAttributes(execution, responseBody, Constants.PROCESS_FLOW_SUCCESS);
            return responseBody;
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT + "{}", ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution,
                    uri + Constants.FAILED_TO_CONNECT + ex.getMessage(), Constants.PROCESS_FLOW_ERROR);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error("failure response from remote entity: {}", ex.getResponseBodyAsString());
            setProcessflowExceptionResponseAttributes(execution, ex.getResponseBodyAsString(),
                    String.valueOf(ex.getRawStatusCode()));
        } catch (MalformedURLException | URISyntaxException ex) {
            setProcessflowExceptionResponseAttributes(execution, ex.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
        return null;
    }

    /**
     * Returns base HTTP header.
     * @param execution execution
     * @return http headers
     */
    public HttpHeaders getBaseHttpHeader(DelegateExecution execution) {

        HttpHeaders headers = new HttpHeaders();
        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);
        headers.set(Constants.ACCESS_TOKEN, accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
