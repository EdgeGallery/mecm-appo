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
public class ApmAdapterTest {

    private static final Logger logger = LoggerFactory.getLogger(ApmAdapterTest.class);

    @InjectMocks
    ApmAdapter apmAdapter = new ApmAdapter();

    @Mock
    ExecutionImpl execution;

    @Test
    public void testExcuteApmAdapter() {
        try {
            Mockito.when(execution.getVariable(AppoConstantsTest.Operation_Type))
                    .thenReturn(AppoConstantsTest.Download);
            apmAdapter.execute(execution);
        } catch (Exception e) {
            logger.error("testExcuteApmAdapter failed: {}", e.getMessage());
        }
    }
}
