package org.edgegallery.mecm.appo.bpmn.tasks;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ProcessflowResponseTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessflowResponseTest.class);

    @InjectMocks
    ProcessflowResponse processflowResponse = new ProcessflowResponse();

    @Mock
    ExecutionImpl execution;

    @Test
    public void testExecuteSuccess() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Response_Type)).thenReturn("success");
        Mockito.when(execution.getVariable(AppoConstantsTest.Response)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Response_Code)).thenReturn(AppoConstantsTest.Applcm_Port);

        try {
            processflowResponse.execute(execution);
        } catch (Exception e) {
            logger.error("executeSuccess failed: {}", e.getMessage());
        }
    }

    @Test
    public void testExecuteFailure() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Response_Type)).thenReturn("failure");
        Mockito.when(execution.getVariable(AppoConstantsTest.Response_Code)).thenReturn(AppoConstantsTest.Applcm_Port);
        try {
            processflowResponse.execute(execution);
        } catch (Exception e) {
            logger.error("testExecuteFailure failed: {}", e.getMessage());
        }
    }

}
