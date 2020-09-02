package org.edgegallery.mecm.appo.bpmn.tasks;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MepmAdapterTest {

    @InjectMocks
    MepmAdapter mepmAdapter;

    @Mock
    ExecutionImpl execution;

    @Test
    public void testInstantiate() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("instantiate");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        assertThrows(Exception.class, () -> {
            mepmAdapter.execute(execution);
        });
    }

    @Test
    public void testQuery() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("query");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        assertThrows(Exception.class, () -> {
            mepmAdapter.execute(execution);
        });
    }

    @Test
    public void testTerminate() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("terminate");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        assertThrows(Exception.class, () -> {
            mepmAdapter.execute(execution);
        });
    }

    @Test
    public void testQuerykpi() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("querykpi");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        Mockito.when(execution.getVariable(AppoConstantsTest.Access_Token)).thenReturn(AppoConstantsTest.Access_Token);
        assertThrows(Exception.class, () -> {
            mepmAdapter.execute(execution);
        });
    }

    @Test
    public void testQueryEdgeCapabilities() {
        Mockito.when(execution.getVariable("action")).thenReturn("queryEdgeCapabilities");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        Mockito.when(execution.getVariable(AppoConstantsTest.Access_Token)).thenReturn(AppoConstantsTest.Access_Token);
        assertThrows(Exception.class, () -> {
            mepmAdapter.execute(execution);
        });
    }
}
