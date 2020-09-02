package org.edgegallery.mecm.appo.exception;

import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class AppoExceptionHandlerTest {

    @InjectMocks
    AppoExceptionHandler appoExceptionHandler = new AppoExceptionHandler();

    @Mock
    AppoException appoException;

    @Test
    public void testAppoException() {
        appoException = new AppoException(AppoConstantsTest.Message);
        appoExceptionHandler.exception(appoException);
    }
}
