/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.mecm.appo.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@RunWith(MockitoJUnitRunner.class)
public class AppoExceptionHandlerTest {

    @InjectMocks
    AppoExceptionHandler appoExceptionHandler = new AppoExceptionHandler();

    @Mock
    AppoException appoException;

    @Mock
    AppoProcessflowException appoProcessflowException;

    @Mock
    MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    MethodParameter parameter;

    @Mock
    BindingResult bindingResult;

    @Mock
    ObjectError var1;

    @Mock
    IllegalArgumentException illegalArgumentException;

    @Mock
    NoSuchElementException noSuchElementException;

    @Mock
    ConstraintViolationException constraintViolationException;

    @Mock
    RuntimeException runtimeException;

    @Mock
    org.springframework.security.access.AccessDeniedException accessDeniedException;

    @Test
    public void testAppoException() {
        appoException = new AppoException(AppoConstantsTest.MESSAGE);
        ResponseEntity<String> appoString = appoExceptionHandler.exception(appoException);
        assertNotNull(appoString);
    }

    @Test
    public void testAppoProcessException() {
        appoProcessflowException = new AppoProcessflowException(AppoConstantsTest.MESSAGE);
        ResponseEntity<String> appoProcessString = appoExceptionHandler.exception(appoProcessflowException);
        assertNotNull(appoProcessString);
    }

    @Test
    public void testMethodArgumentException() {
        bindingResult.addError(var1);
        methodArgumentNotValidException = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<AppoExceptionResponse> appoResponse =
                appoExceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException);
        assertNotNull(appoResponse);
    }

    @Test
    public void testIllegalArgumentException() {
        illegalArgumentException = new IllegalArgumentException();
        ResponseEntity<AppoExceptionResponse> appoHandlerResponse = appoExceptionHandler
                .handleIllegalArgException(illegalArgumentException);
        assertNotNull(appoHandlerResponse);
    }

    @Test
    public void testNoSuchElementException() {
        noSuchElementException = new NoSuchElementException();
        ResponseEntity<AppoExceptionResponse> appoElementResponse =
                appoExceptionHandler.handleNoSuchElementException(noSuchElementException);
        assertNotNull(appoElementResponse);
    }

    @Test
    public void testHandleConstraintViolationException() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation mockedViolation = mock(ConstraintViolation.class);
        violations.add(mockedViolation);
        constraintViolationException = new ConstraintViolationException("message", violations);
        ResponseEntity<AppoExceptionResponse> appoConstraintViolationResponse =
                appoExceptionHandler.handleConstraintViolationException(constraintViolationException);
        assertNotNull(appoConstraintViolationResponse);
    }

    @Test
    public void testHandleRuntimeException() {
        runtimeException = new RuntimeException();
        ResponseEntity<AppoExceptionResponse> appoRuntimeResponse =
                appoExceptionHandler.handleRuntimeException(runtimeException);
        assertNotNull(appoRuntimeResponse);
    }

    @Test
    public void testHandleAccessDeniedException() {
        accessDeniedException = new AccessDeniedException(AppoConstantsTest.MESSAGE);
        ResponseEntity<AppoExceptionResponse> appoAccessDeniedResponse =
                appoExceptionHandler.handleAccessDeniedException(accessDeniedException);
        assertNotNull(appoAccessDeniedResponse);
    }
}
