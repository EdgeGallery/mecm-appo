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

package org.edgegallery.mecm.appo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.edgegallery.mecm.appo.common.AppoProcessFlowResponse;
import org.edgegallery.mecm.appo.exception.AppoProcessflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AppoProcessflowServiceImpl extends AppoProcessEngineService implements AppoProcessflowService {

    private static final Logger logger = LoggerFactory.getLogger(AppoProcessflowServiceImpl.class);

    /**
     * Generate request ID.
     *
     * @param inputVariables request input variables
     * @return unique id
     */
    protected static String getRequestID(Map<String, Object> inputVariables) {
        String value = Objects.toString(inputVariables.get("request_id"), null);
        if (value == null) {
            value = UUID.randomUUID().toString();
            inputVariables.put("request_id", value);
        }
        return value;
    }

    /**
     * Converts input map to object map.
     *
     * @param requestInput request input
     * @return converted object map
     */
    protected static Map<String, Object> convertInputToObjectMap(Map<String, String> requestInput) {
        HashMap<String, Object> variables = new HashMap<>();
        Set<Entry<String, String>> entries = requestInput.entrySet();
        for (Map.Entry<String, String> mapEntry : entries) {
            String name = mapEntry.getKey();
            Object value = mapEntry.getValue();
            variables.put(name, value);
        }
        return variables;
    }

    @Override
    @Async
    public void executeProcessAsync(String processKey, Map<String, String> requestInput) {
        logger.debug("Received Application orchestration request: processKey: {}", processKey);

        String processInstanceId = null;
        try {
            Map<String, Object> wfInputParmas;
            wfInputParmas = convertInputToObjectMap(requestInput);

            String requestID = getRequestID(wfInputParmas);

            RuntimeService runtimeService = getEngineServices().getRuntimeService();
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(processKey, requestID, wfInputParmas);
            processInstanceId = processInstance.getId();

            String processInstanceState = "RUNNING";
            if (processInstance.isEnded()) {
                processInstanceState = "ENDED";
            }

            logger.debug("processKey: {}  processInstanceId: {} Status: {} ", processKey,
                    processInstanceId, processInstanceState);
        } catch (Exception e) {
            AppoProcessFlowResponse appoProcessFlowResponse = new AppoProcessFlowResponse();
            appoProcessFlowResponse.setResponse("Error occurred while executing the process: " + e.getMessage());
            appoProcessFlowResponse.setProcessInstanceID(processInstanceId);
            appoProcessFlowResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw new AppoProcessflowException(appoProcessFlowResponse);
        }
    }

    @Override
    public AppoProcessFlowResponse executeProcessSync(String processKey, Map<String, String> requestInput) {
        logger.debug("Received Application orchestration request: processKey: {}", processKey);

        String processInstanceId = null;
        AppoProcessFlowResponse appoProcessFlowResponse = null;
        try {
            Map<String, Object> wfInputParmas;
            wfInputParmas = convertInputToObjectMap(requestInput);

            String requestID = getRequestID(wfInputParmas);

            RuntimeService runtimeService = getEngineServices().getRuntimeService();
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(processKey, requestID, wfInputParmas);
            processInstanceId = processInstance.getId();

            String processInstanceState = "RUNNING";
            if (processInstance.isEnded()) {
                processInstanceState = "ENDED";
            }
            logger.debug("processName: {}  processInstanceId: {} Status: {} ", processKey, processInstanceId,
                    processInstanceState);

            appoProcessFlowResponse = getProcessInstanceData(processInstance);
            if (appoProcessFlowResponse != null) {
                appoProcessFlowResponse.setProcessInstanceID(processInstanceId);
            }
        } catch (Exception e) {
            appoProcessFlowResponse = new AppoProcessFlowResponse();
            String response = "Workflow execution failed due to error during execution : " + e.getMessage();
            appoProcessFlowResponse.setResponse(response);
            appoProcessFlowResponse.setProcessInstanceID(processInstanceId);
            appoProcessFlowResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw new AppoProcessflowException(appoProcessFlowResponse);
        }
        return appoProcessFlowResponse;
    }

    /**
     * Retrieves process instance data for response processing.
     *
     * @param processInstance process instance
     * @return processflow response
     */
    private AppoProcessFlowResponse getProcessInstanceData(ProcessInstance processInstance) {

        // Retrieve response variables from history service.
        String processInstanceId = processInstance.getId();

        // Retrieve process instance data. processflowResponse and processflowResponseCode
        String responseCode = getProcessflowResponse(processInstanceId, "ProcessflowResponseCode");

        AppoProcessFlowResponse appoProcessFlowResponse = processflowResponse(processInstanceId, responseCode);
        if (appoProcessFlowResponse != null) {
            return appoProcessFlowResponse;
        }

        AppoProcessFlowResponse appoProcessExceptionResponse = processflowException(processInstanceId, responseCode);
        if (appoProcessExceptionResponse != null) {
            return appoProcessExceptionResponse;
        }

        return processflowErrorResponse(processInstanceId, responseCode);
    }

    /**
     * Retrieves process response from history service.
     *
     * @param processInstanceId process instance ID
     * @param responseCode      response code
     * @return process flow response on success, null if response not available
     */
    private AppoProcessFlowResponse processflowResponse(String processInstanceId,
                                                        String responseCode) {
        AppoProcessFlowResponse appoProcessFlowResponse = new AppoProcessFlowResponse();

        String response = getProcessflowResponse(processInstanceId, "ProcessflowResponse");
        if (response != null) {
            appoProcessFlowResponse.setResponse(response);
            appoProcessFlowResponse.setResponseCode(Integer.parseInt(responseCode));
            return appoProcessFlowResponse;
        }
        return null;
    }

    /**
     * Retrieves process exception response from history service.
     *
     * @param processInstanceId process instance ID
     * @param responseCode      response code
     * @return process flow response on success, null if response not available
     */
    private AppoProcessFlowResponse processflowException(String processInstanceId,
                                                         String responseCode) {
        AppoProcessFlowResponse appoProcessFlowResponse = new AppoProcessFlowResponse();

        Object exceptionObject = getWorkflowResponseObject(processInstanceId, "ProcessflowException");
        if (exceptionObject != null) {
            String exceptionResponse = null;
            if (exceptionObject instanceof AppoProcessflowException) {
                AppoProcessflowException exception = (AppoProcessflowException) exceptionObject;
                exceptionResponse = exception.toString();

                appoProcessFlowResponse.setResponse(exceptionResponse);
                appoProcessFlowResponse.setResponseCode(Integer.parseInt(exception.getErrorCode().toString()));
                return appoProcessFlowResponse;
            } else if (exceptionObject instanceof String) {

                exceptionResponse = (String) exceptionObject;
                appoProcessFlowResponse.setResponse(exceptionResponse);
                appoProcessFlowResponse.setResponseCode(Integer.parseInt(responseCode));
                return appoProcessFlowResponse;
            }
        }
        return null;
    }

    /**
     * Retrieves process error response from history service.
     *
     * @param processInstanceId process instance ID
     * @param responseCode      response code
     * @return process flow response on success, null if response not available
     */
    private AppoProcessFlowResponse processflowErrorResponse(String processInstanceId,
                                                             String responseCode) {

        AppoProcessFlowResponse appoProcessFlowResponse = new AppoProcessFlowResponse();

        String errResponse = getProcessflowResponse(processInstanceId, "ProcessflowErrResponse");
        if (errResponse != null) {
            appoProcessFlowResponse.setResponse(errResponse);
            appoProcessFlowResponse.setResponseCode(Integer.parseInt(responseCode));
            return appoProcessFlowResponse;
        }

        if (responseCode != null) {
            appoProcessFlowResponse.setResponseCode(Integer.parseInt(responseCode));
            return appoProcessFlowResponse;
        }
        return null;
    }

    /**
     * Retrieve process instance variable from history.
     *
     * @param processInstanceId process instance ID
     * @param variableName      variable name to obtain for process instance history
     * @return variable value, null if not available
     */
    private String getProcessflowResponse(String processInstanceId, String variableName) {

        Object responseData = getProcessInstanceVariable(processInstanceId, variableName);
        String response = responseData == null ? null : String.valueOf(responseData);
        logger.debug("processInstId: {} %p processInstanceData: %p           {} : {}",
                processInstanceId, variableName, response);
        return response;
    }

    /**
     * Retrieve process instance variable object from history.
     *
     * @param processInstanceId process instance ID
     * @param variableName      variable name to obtain for process instance history
     * @return variable value, null if not available
     */
    private Object getWorkflowResponseObject(String processInstanceId, String variableName) {

        Object responseData = getProcessInstanceVariable(processInstanceId, variableName);
        if (responseData != null) {
            String response = String.valueOf(responseData);

            logger.debug("processInstId: {} %p processInstanceData: %p           {} : {}",
                    processInstanceId, variableName, response);
        }
        return responseData;
    }

    /**
     * Retrieve process instance variable from history service.
     *
     * @param processInstanceId process instance ID
     * @param variableName      variable name to retrieve
     * @return variable object on success, null on error
     */
    private Object getProcessInstanceVariable(String processInstanceId, String variableName) {

        HistoryService historyService = getEngineServices().getHistoryService();

        try {
            HistoricVariableInstance v = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId).variableName(variableName).singleResult();
            if (v != null) {
                return v.getValue();
            }
            return null;
        } catch (Exception e) {
            logger.debug("Failed to retrieve variable {} from process instance {}: {}", variableName,
                    processInstanceId, e.getMessage());
            return null;
        }
    }
}

