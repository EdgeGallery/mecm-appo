package org.edgegallery.mecm.appo.bpmn.tasks;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeComposeApplicationPackageTest {

    private static final Logger logger = LoggerFactory.getLogger(DeComposeApplicationPackageTest.class);

    @InjectMocks
    DeComposeApplicationPackage deComposeApplicationPackage = new DeComposeApplicationPackage();

    @Mock
    ExecutionImpl execution;

    @Test
    public void testExcuteDeComposeApplicationPackage() {

        try {
            deComposeApplicationPackage.execute(execution);
        } catch (Exception e) {
            logger.error("testExcuteDeComposeApplicationPackage failed : {}", e.getMessage());
        }
    }

}
