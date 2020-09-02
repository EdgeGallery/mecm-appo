package org.edgegallery.mecm.appo.bpmn.tasks;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessflowResponseTest {

    @InjectMocks
    ProcessflowResponse processflowResponse = new ProcessflowResponse();

    @Mock
    ExecutionImpl execution;

    @Test
    public void testExecuteSuccess() throws Exception {
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_TYPE)).thenReturn("success");
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE)).thenReturn(AppoConstantsTest.APPLCM_IP);
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_CODE)).thenReturn(AppoConstantsTest.APPLCM_PORT);
        processflowResponse.execute(execution);
    }

    @Test
    public void testExecuteFailure() throws Exception {
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_TYPE)).thenReturn("failure");
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_CODE)).thenReturn(AppoConstantsTest.APPLCM_PORT);
        processflowResponse.execute(execution);
    }
}
