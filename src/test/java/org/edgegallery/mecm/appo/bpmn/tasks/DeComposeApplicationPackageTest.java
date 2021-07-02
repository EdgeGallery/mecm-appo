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

import static org.edgegallery.mecm.appo.bpmn.tasks.ProcessflowAbstractTask.RESPONSE_CODE;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.impl.AppInstanceInfoServiceImpl;
import org.edgegallery.mecm.appo.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeComposeApplicationPackageTest {

    @InjectMocks
    DeComposeApplicationPackage deComposeApplicationPackage = new DeComposeApplicationPackage();

    @Mock
    ExecutionImpl execution;

    @Mock
    AppInstanceInfoServiceImpl appInstanceInfoService;

    @Before
    public void setUp() throws Exception {
        Field field = deComposeApplicationPackage.getClass().getDeclaredField("appPkgBasesPath");
        field.setAccessible(true);
        field.set(deComposeApplicationPackage, "src/test/");
    }

    @Test
    public void testNoRequire() throws Exception {
        Mockito.when(execution.getVariable(Constants.APP_PACKAGE_ID))
                .thenReturn("22406fba-fd5d-4f55-b3fa-89a45fee913a");
        Mockito.when(execution.getVariable(Constants.MEC_HOST)).thenReturn(AppoConstantsTest.MEC_HOST);
        Mockito.when(execution.getVariable(Constants.APP_INSTANCE_ID))
                .thenReturn("resources");
        Mockito.when(execution.getVariable(Constants.APP_ID))
                .thenReturn(AppoConstantsTest.APP_ID);
        Mockito.when(execution.getVariable(Constants.TENANT_ID)).thenReturn(AppoConstantsTest.TENANT);
        Map<String, Boolean> result = new HashMap<>();
        Mockito.doAnswer(invocationOnMock -> result.put("deploySuccess", true))
                .when(execution).setVariable(RESPONSE_CODE, Constants.PROCESS_FLOW_SUCCESS);

        Assertions.assertDoesNotThrow(() -> deComposeApplicationPackage.execute(execution));
        Assertions.assertTrue(result.containsKey("deploySuccess"));
    }

    @Test
    public void testRequiredAndDeployFailure() throws Exception {
        Mockito.when(execution.getVariable(Constants.APP_PACKAGE_ID))
                .thenReturn("22406fba-fd5d-4f55-b3fa-89a45fee913a-apprules");
        Mockito.when(execution.getVariable(Constants.MEC_HOST)).thenReturn(AppoConstantsTest.MEC_HOST);
        Mockito.when(execution.getVariable(Constants.APP_INSTANCE_ID))
                .thenReturn("resources");
        Mockito.when(execution.getVariable(Constants.APP_ID))
                .thenReturn(AppoConstantsTest.APP_ID);
        Mockito.when(execution.getVariable(Constants.TENANT_ID)).thenReturn(AppoConstantsTest.TENANT);
        Map<String, Boolean> result = new HashMap<>();
        Mockito.doAnswer(invocationOnMock -> result.put("deployFailure", true))
                .when(execution).setVariable(RESPONSE_CODE, Constants.PROCESS_FLOW_ERROR);

        Assertions.assertDoesNotThrow(() -> deComposeApplicationPackage.execute(execution));
        Assertions.assertTrue(result.containsKey("deployFailure"));
    }

    @Test
    public void testRequiredAndDeploySuccess() throws Exception {
        AppInstanceInfo appInstanceInfo = new AppInstanceInfo();
        appInstanceInfo.setOperationalStatus(Constants.OPER_STATUS_INSTANTIATED);
        appInstanceInfo.setAppId("ea8ebc1a-db88-11ea-87d0-0242ac130003");
        appInstanceInfo.setAppPackageId("b1bb0ce7-ebca-4fa7-95ed-4840d70a1177");
        appInstanceInfo.setAppName("face-recognize");
        appInstanceInfo.setAppInstanceId("required_instance");
        appInstanceInfo.setMecHost(AppoConstantsTest.MEC_HOST);
        List<AppInstanceInfo> deployList = new ArrayList<>();
        deployList.add(appInstanceInfo);

        Mockito.when(execution.getVariable(Constants.APP_PACKAGE_ID))
                .thenReturn("22406fba-fd5d-4f55-b3fa-89a45fee913a-apprules");
        Mockito.when(execution.getVariable(Constants.MEC_HOST)).thenReturn(AppoConstantsTest.MEC_HOST);
        Mockito.when(execution.getVariable(Constants.APP_INSTANCE_ID))
                .thenReturn("resources");
        Mockito.when(execution.getVariable(Constants.APP_ID))
                .thenReturn(AppoConstantsTest.APP_ID);
        Mockito.when(execution.getVariable(Constants.TENANT_ID)).thenReturn(AppoConstantsTest.TENANT_ID);
        when(appInstanceInfoService.getAppInstanceInfoByMecHost(AppoConstantsTest.TENANT_ID, AppoConstantsTest.MEC_HOST))
                .thenReturn(deployList);

        Map<String, Boolean> result = new HashMap<>();
        Mockito.doAnswer(invocationOnMock -> result.put("deploySuccess", true))
                .when(execution).setVariable(RESPONSE_CODE, Constants.PROCESS_FLOW_SUCCESS);

        Assertions.assertDoesNotThrow(() -> deComposeApplicationPackage.execute(execution));
        Assertions.assertTrue(result.containsKey("deploySuccess"));
    }
}
