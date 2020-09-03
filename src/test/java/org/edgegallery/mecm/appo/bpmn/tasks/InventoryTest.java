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
import org.edgegallery.mecm.appo.service.AppoRestClientService;
import org.edgegallery.mecm.appo.utils.AppoRestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InventoryTest {

    @InjectMocks
    Inventory inventory;

    @Mock
    AppoRestClientService appoRestClientService;

    @Mock
    ExecutionImpl execution;

    @Mock
    AppoRestClient appoRestClient;

    @Test
    public void testMecHost() {
        Mockito.when(execution.getVariable("inventory")).thenReturn("mecHost");
        Mockito.when(appoRestClientService.getAppoRestClient()).thenReturn(appoRestClient);
        inventory = new Inventory(execution, "30000", appoRestClientService);
        Mockito.when(execution.getVariable("tenant_id")).thenReturn(AppoConstantsTest.TENANT_ID);
        Mockito.when(execution.getVariable(AppoConstantsTest.MEC_HOST)).thenReturn(AppoConstantsTest.MEC_HOST);
        Mockito.when(execution.getVariable(AppoConstantsTest.ACCESS_TOKEN)).thenReturn(AppoConstantsTest.ACCESS_TOKEN);
        inventory.execute();
    }

    @Test
    public void testApplcm() {
        Mockito.when(execution.getVariable("inventory")).thenReturn("applcm");
        Mockito.when(appoRestClientService.getAppoRestClient()).thenReturn(appoRestClient);
        inventory = new Inventory(execution, "30000", appoRestClientService);
        Mockito.when(execution.getVariable("tenant_id")).thenReturn(AppoConstantsTest.TENANT_ID);
        Mockito.when(execution.getVariable("applcm_ip")).thenReturn(AppoConstantsTest.MEC_HOST);
        Mockito.when(execution.getVariable(AppoConstantsTest.ACCESS_TOKEN)).thenReturn(AppoConstantsTest.ACCESS_TOKEN);
        inventory.execute();
    }
}