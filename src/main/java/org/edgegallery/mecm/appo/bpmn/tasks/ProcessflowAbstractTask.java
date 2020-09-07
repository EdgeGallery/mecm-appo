package org.edgegallery.mecm.appo.bpmn.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOGGER.info("Set process flow response, response: {} response code: {}", response, responseCode);
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
        LOGGER.info("Set process flow error response, response: {} response code: {}", response, responseCode);
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
        LOGGER.info("Set process flow exception response, response: {} response code: {}", response, responseCode);
        delegateExecution.setVariable(RESPONSE_CODE, responseCode);
        delegateExecution.setVariable(FLOW_EXCEPTION, response);
        delegateExecution.setVariable(ERROR_RESPONSE, response);
    }
}
