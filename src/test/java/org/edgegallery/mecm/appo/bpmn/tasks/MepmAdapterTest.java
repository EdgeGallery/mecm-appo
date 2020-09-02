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
public class MepmAdapterTest {

    private static final Logger logger = LoggerFactory.getLogger(MepmAdapterTest.class);

    @InjectMocks
    MepmAdapter mepmAdapter;

    @Mock
    ExecutionImpl execution;

    @Test
    public void testInstantiate() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("instantiate");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        try {
            mepmAdapter.execute(execution);
        } catch (Exception e) {
            logger.info("testInstantiate failed: {}", e.getMessage());
        }
    }

    @Test
    public void testQuery() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("query");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        try {
            mepmAdapter.execute(execution);
        } catch (Exception e) {
            logger.info("testQuery failed: {}", e.getMessage());
        }
    }

    @Test
    public void testTerminate() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("terminate");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        try {
            mepmAdapter.execute(execution);
        } catch (Exception e) {
            logger.info("testTerminate failed: {}", e.getMessage());
        }
    }

    @Test
    public void testQuerykpi() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Action)).thenReturn("querykpi");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        Mockito.when(execution.getVariable(AppoConstantsTest.Access_Token)).thenReturn(AppoConstantsTest.Access_Token);
        try {
            mepmAdapter.execute(execution);
        } catch (Exception e) {
            logger.info("testQuerykpi failed: {}", e.getMessage());
        }
    }

    @Test
    public void testQueryEdgeCapabilities() {
        Mockito.when(execution.getVariable("action")).thenReturn("queryEdgeCapabilities");
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Ip)).thenReturn(AppoConstantsTest.Applcm_Ip);
        Mockito.when(execution.getVariable(AppoConstantsTest.Applcm_Port)).thenReturn(AppoConstantsTest.Applcm_Port);
        Mockito.when(execution.getVariable(AppoConstantsTest.Access_Token)).thenReturn(AppoConstantsTest.Access_Token);
        try {
            mepmAdapter.execute(execution);
        } catch (Exception e) {
            logger.info("testQueryEdgeCapabilities failed: {}", e.getMessage());
        }
    }
}
