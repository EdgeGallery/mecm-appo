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

            //Generate request ID if not provided in request
            String requestID = getRequestID(wfInputParmas);

            RuntimeService runtimeService = getEngineServices().getRuntimeService();
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(processKey, requestID, wfInputParmas);
            processInstanceId = processInstance.getId();

            logger.debug("processKey: {}  processInstanceId: {} Status: {} ", processKey, processInstanceId,
                    (processInstance.isEnded() ? "ENDED" : "RUNNING"));
        } catch (Exception e) {
            AppoProcessFlowResponse appoProcessFlowResponse = new AppoProcessFlowResponse();
            appoProcessFlowResponse.setResponse("Error occurred while executing the process: " + e.getMessage());
            appoProcessFlowResponse.setProcessInstanceID(processInstanceId);
            appoProcessFlowResponse.setResponseCode(500);
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

            //Generate request ID if not provided in request
            String requestID = getRequestID(wfInputParmas);

            RuntimeService runtimeService = getEngineServices().getRuntimeService();
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(processKey, requestID, wfInputParmas);
            processInstanceId = processInstance.getId();

            logger.debug("processName: {}  processInstanceId: {} Status: {} ", processKey, processInstanceId,
                    (processInstance.isEnded() ? "ENDED" : "RUNNING"));

            appoProcessFlowResponse = getProcessInstanceData(processInstance);
            if (appoProcessFlowResponse != null) {
                appoProcessFlowResponse.setProcessInstanceID(processInstanceId);
            }
        } catch (Exception e) {
            appoProcessFlowResponse = new AppoProcessFlowResponse();
            String response = "Workflow execution failed due to error during execution : " + e.getMessage();
            appoProcessFlowResponse.setResponse(response);
            appoProcessFlowResponse.setProcessInstanceID(processInstanceId);
            appoProcessFlowResponse.setResponseCode(500);
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

        AppoProcessFlowResponse appoProcessFlowResponse = new AppoProcessFlowResponse();

        // Retrieve response variables from history service.
        String processInstanceId = processInstance.getId();

        //Retrieve process instance data. processflowResponse and processflowResponseCode
        HistoryService historyService = getEngineServices().getHistoryService();
        String responseCode = getProcessflowResponse(historyService, processInstanceId,
                "ProcessflowResponseCode");

        String response = getProcessflowResponse(historyService, processInstanceId, "ProcessflowResponse");
        if (response != null) {
            appoProcessFlowResponse.setResponse(response);
            appoProcessFlowResponse.setResponseCode(Integer.parseInt(responseCode));
            return appoProcessFlowResponse;
        }

        Object exceptionObject = getWorkflowResponseObject(historyService,
                processInstanceId, "ProcessflowException");
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

        String errResponse = getProcessflowResponse(historyService, processInstanceId,
                "ProcessflowErrResponse");
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
     * @param historyService    history service
     * @param processInstanceId process instance ID
     * @param variableName      variable name to obtain for process instance history
     * @return variable value, null if not available
     */
    private String getProcessflowResponse(HistoryService historyService, String processInstanceId,
                                          String variableName) {
        Object responseData = getProcessInstanceVariable(historyService, processInstanceId, variableName);
        String response = responseData == null ? null : String.valueOf(responseData);
        logger.debug("processInstId: {} %p processInstanceData: %p           {} : {}",
                processInstanceId, variableName, response);
        return response;
    }

    /**
     * Retrieve process instance variable object from history.
     *
     * @param historyService    history service
     * @param processInstanceId process instance ID
     * @param variableName      variable name to obtain for process instance history
     * @return variable value, null if not available
     */
    private Object getWorkflowResponseObject(HistoryService historyService, String processInstanceId,
                                             String variableName) {
        Object responseData = getProcessInstanceVariable(historyService, processInstanceId, variableName);
        String response = responseData == null ? null : String.valueOf(responseData);
        logger.debug("processInstId: {} %p processInstanceData: %p           {} : {}",
                processInstanceId, variableName, response);
        return responseData;
    }

    /**
     * Retrieve process instance variable from history service.
     *
     * @param historyService    history service
     * @param processInstanceId process instance ID
     * @param variableName      variable name to retrieve
     * @return variable object on success, null on error
     */
    private Object getProcessInstanceVariable(HistoryService historyService, String processInstanceId,
                                              String variableName) {
        try {
            HistoricVariableInstance v = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId).variableName(variableName).singleResult();
            return v == null ? null : v.getValue();
        } catch (Exception e) {
            logger.debug("Failed to retrieve variable {} from process instance {}: {}", variableName,
                    processInstanceId, e.getMessage());
            return null;
        }
    }
}

