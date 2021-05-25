/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.mecm.appo.bpmn.tasks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE)).thenReturn(AppoConstantsTest.MEPM_IP);
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_CODE)).thenReturn(AppoConstantsTest.MEPM_PORT);
        assertDoesNotThrow(() -> processflowResponse.execute(execution));
    }

    @Test
    public void testExecuteFailure() throws Exception {
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_TYPE)).thenReturn("failure");
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_CODE)).thenReturn(AppoConstantsTest.MEPM_PORT);
        assertDoesNotThrow(() -> processflowResponse.execute(execution));
    }
}
