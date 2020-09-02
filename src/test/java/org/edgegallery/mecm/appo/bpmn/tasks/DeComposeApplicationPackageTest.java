package org.edgegallery.mecm.appo.bpmn.tasks;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class DeComposeApplicationPackageTest {

    @InjectMocks
    DeComposeApplicationPackage deComposeApplicationPackage = new DeComposeApplicationPackage();

    @Mock
    ExecutionImpl execution;

    @Test
    public void testExcuteDeComposeApplicationPackage() throws Exception {
        deComposeApplicationPackage.execute(execution);
    }
}
