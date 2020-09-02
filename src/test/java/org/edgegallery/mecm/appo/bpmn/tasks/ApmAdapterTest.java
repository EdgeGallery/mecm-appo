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
public class ApmAdapterTest {

    @InjectMocks
    ApmAdapter apmAdapter = new ApmAdapter();

    @Mock
    ExecutionImpl execution;

    @Test
    public void testExcuteApmAdapter() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Operation_Type))
                .thenReturn(AppoConstantsTest.Download);
        assertThrows(Exception.class, () -> {
            apmAdapter.execute(execution);
        });
    }
}
