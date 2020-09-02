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

package org.edgegallery.mecm.appo.bpmn.tasks;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetRequestInputsTest {

    @InjectMocks
    GetRequestInputs getRequestInputs;

    @Mock
    ExecutionImpl execution;

    @Test
    public void InputParametersCreateAppInstance() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Request_Action)).thenReturn("CreateAppInstance");
        Mockito.when(execution.getVariable(AppoConstantsTest.Access_Token)).thenReturn(AppoConstantsTest.Access_Token);
        Mockito.when(execution.getVariable(AppoConstantsTest.App_Package_Id))
                .thenReturn(AppoConstantsTest.App_Package_Id);
        Mockito.when(execution.getVariable(AppoConstantsTest.Mec_Host)).thenReturn(AppoConstantsTest.Mec_Host);
        Mockito.when(execution.getVariable(AppoConstantsTest.App_Instance_Descripton))
                .thenReturn(AppoConstantsTest.App_Instance_Descripton);
        Mockito.when(execution.getVariable(AppoConstantsTest.App_Name)).thenReturn(AppoConstantsTest.App_Name);
        Mockito.when(execution.getVariable(AppoConstantsTest.App_Instance_Id))
                .thenReturn(AppoConstantsTest.App_Instance_Id);
        getRequestInputs.execute(execution);
    }

    @Test
    public void InputParametersQuery() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Request_Action)).thenReturn("QueryAppInstance");
        Mockito.when(execution.getVariable(AppoConstantsTest.Access_Token)).thenReturn(AppoConstantsTest.Access_Token);
        getRequestInputs.execute(execution);
    }

    @Test
    public void InputParametersQueryKPI() {
        Mockito.when(execution.getVariable(AppoConstantsTest.Request_Action)).thenReturn("QueryKPI");
        Mockito.when(execution.getVariable(AppoConstantsTest.Access_Token)).thenReturn(AppoConstantsTest.Access_Token);
        getRequestInputs.execute(execution);
    }
}
