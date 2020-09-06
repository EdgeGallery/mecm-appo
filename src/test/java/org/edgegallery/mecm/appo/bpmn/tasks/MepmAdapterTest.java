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
public class MepmAdapterTest {

    @InjectMocks
    MepmAdapter mepmAdapter;

    @Mock
    ExecutionImpl execution;

    @Test
    public void testInstantiate() throws Exception {
        Mockito.when(execution.getVariable(AppoConstantsTest.ACTION)).thenReturn("instantiate");
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_IP)).thenReturn(AppoConstantsTest.APPLCM_IP);
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_PORT)).thenReturn(AppoConstantsTest.APPLCM_PORT);
        mepmAdapter.execute(execution);
    }

    @Test
    public void testQuery() throws Exception {
        Mockito.when(execution.getVariable(AppoConstantsTest.ACTION)).thenReturn("query");
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_IP)).thenReturn(AppoConstantsTest.APPLCM_IP);
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_PORT)).thenReturn(AppoConstantsTest.APPLCM_PORT);
        mepmAdapter.execute(execution);
    }

    @Test
    public void testTerminate() throws Exception {
        Mockito.when(execution.getVariable(AppoConstantsTest.ACTION)).thenReturn("terminate");
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_IP)).thenReturn(AppoConstantsTest.APPLCM_IP);
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_PORT)).thenReturn(AppoConstantsTest.APPLCM_PORT);
        mepmAdapter.execute(execution);
    }

    @Test
    public void testQuerykpi() throws Exception {
        Mockito.when(execution.getVariable(AppoConstantsTest.ACTION)).thenReturn("querykpi");
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_IP)).thenReturn(AppoConstantsTest.APPLCM_IP);
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_PORT)).thenReturn(AppoConstantsTest.APPLCM_PORT);
        mepmAdapter.execute(execution);
    }

    @Test
    public void testQueryEdgeCapabilities() throws Exception {
        Mockito.when(execution.getVariable("action")).thenReturn("queryEdgeCapabilities");
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_IP)).thenReturn(AppoConstantsTest.APPLCM_IP);
        Mockito.when(execution.getVariable(AppoConstantsTest.APPLCM_PORT)).thenReturn(AppoConstantsTest.APPLCM_PORT);
        mepmAdapter.execute(execution);
    }
}
