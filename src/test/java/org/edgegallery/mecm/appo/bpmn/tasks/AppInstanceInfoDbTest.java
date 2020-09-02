/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.edgegallery.mecm.appo.service.AppInstanceInfoServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppInstanceInfoDbTest {

    @InjectMocks
    AppInstanceInfoDb appoExceptionHandler;

    @Mock
    AppInstanceInfoServiceImpl appInstanceInfoServiceImpl;

    @Mock
    ExecutionImpl execution;

    @Test
    public void testInsert() {
        Mockito.when(execution.getVariable(AppoConstantsTest.OPERATION_TYPE)).thenReturn("insert");
        appoExceptionHandler = new AppInstanceInfoDb(execution, appInstanceInfoServiceImpl);
        appoExceptionHandler.execute();
    }

    @Test
    public void testUpdate() {
        Mockito.when(execution.getVariable(AppoConstantsTest.OPERATION_TYPE)).thenReturn("update");
        appoExceptionHandler = new AppInstanceInfoDb(execution, appInstanceInfoServiceImpl);
        Mockito.when(execution.getVariable(AppoConstantsTest.RESPONSE_CODE)).thenReturn("200");
        appoExceptionHandler.execute();
    }

    @Test
    public void testGet() {
        Mockito.when(execution.getVariable(AppoConstantsTest.OPERATION_TYPE)).thenReturn("get");
        appoExceptionHandler = new AppInstanceInfoDb(execution, appInstanceInfoServiceImpl);
        appoExceptionHandler.execute();
    }

    @Test
    public void testDelete() {
        Mockito.when(execution.getVariable(AppoConstantsTest.OPERATION_TYPE)).thenReturn("delete");
        appoExceptionHandler = new AppInstanceInfoDb(execution, appInstanceInfoServiceImpl);
        appoExceptionHandler.execute();
    }
}