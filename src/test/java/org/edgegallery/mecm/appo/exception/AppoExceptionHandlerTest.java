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

import java.util.NoSuchElementException;
import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    @Test
    public void testAppoException() {
        appoException = new AppoException(AppoConstantsTest.MESSAGE);
        appoExceptionHandler.exception(appoException);
    }

    @Test
    public void testAppoProcessException() {
        appoProcessflowException = new AppoProcessflowException(AppoConstantsTest.MESSAGE);
        appoExceptionHandler.exception(appoProcessflowException);
    }

    @Test
    public void testMethodArgumentException() {
        bindingResult.addError(var1);
        methodArgumentNotValidException = new MethodArgumentNotValidException(parameter, bindingResult);
        appoExceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException);
    }

    @Test
    public void testIllegalArgumentException() {
        illegalArgumentException = new IllegalArgumentException();
        appoExceptionHandler.handleIllegalArgException(illegalArgumentException);
    }

    @Test
    public void testNoSuchElementException() {
        noSuchElementException = new NoSuchElementException();
        appoExceptionHandler.handleNoSuchElementException(noSuchElementException);
    }
}
