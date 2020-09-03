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
    public void inputParametersCreateAppInstance() {
        Mockito.when(execution.getVariable(AppoConstantsTest.REQUEST_ACTION)).thenReturn("CreateAppInstance");
        Mockito.when(execution.getVariable(AppoConstantsTest.ACCESS_TOKEN)).thenReturn(AppoConstantsTest.ACCESS_TOKEN);
        Mockito.when(execution.getVariable(AppoConstantsTest.APP_PACKAGE_ID))
                .thenReturn(AppoConstantsTest.APP_PACKAGE_ID);
        Mockito.when(execution.getVariable(AppoConstantsTest.MEC_HOST)).thenReturn(AppoConstantsTest.MEC_HOST);
        Mockito.when(execution.getVariable(AppoConstantsTest.APP_INSTANCE_DESCRIPTION))
                .thenReturn(AppoConstantsTest.APP_INSTANCE_DESCRIPTION);
        Mockito.when(execution.getVariable(AppoConstantsTest.APP_NAME)).thenReturn(AppoConstantsTest.APP_NAME);
        Mockito.when(execution.getVariable(AppoConstantsTest.APP_INSTANCE_ID))
                .thenReturn(AppoConstantsTest.APP_INSTANCE_ID);
        getRequestInputs.execute(execution);
    }

    @Test
    public void inputParametersQuery() {
        Mockito.when(execution.getVariable(AppoConstantsTest.REQUEST_ACTION)).thenReturn("QueryAppInstance");
        Mockito.when(execution.getVariable(AppoConstantsTest.ACCESS_TOKEN)).thenReturn(AppoConstantsTest.ACCESS_TOKEN);
        getRequestInputs.execute(execution);
    }

    @Test
    public void inputParametersQueryKPI() {
        Mockito.when(execution.getVariable(AppoConstantsTest.REQUEST_ACTION)).thenReturn("QueryKPI");
        Mockito.when(execution.getVariable(AppoConstantsTest.ACCESS_TOKEN)).thenReturn(AppoConstantsTest.ACCESS_TOKEN);
        getRequestInputs.execute(execution);
    }
}
