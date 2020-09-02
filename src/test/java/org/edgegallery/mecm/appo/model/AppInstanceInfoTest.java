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

package org.edgegallery.mecm.appo.model;

import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class AppInstanceInfoTest {

    @InjectMocks
    AppInstanceInfo appInstanceInfo = new AppInstanceInfo();

    @Before
    public void setUp() {
        appInstanceInfo.setAppDescriptor(AppoConstantsTest.App_Descriptor);
        appInstanceInfo.setAppdId(AppoConstantsTest.APP_ID);
        appInstanceInfo.setAppInstanceId(AppoConstantsTest.App_Instance_Id);
        appInstanceInfo.setAppName(AppoConstantsTest.App_Name);
        appInstanceInfo.setApplcmHost(AppoConstantsTest.Applcm_LCM_HOST);
        appInstanceInfo.setAppPackageId(AppoConstantsTest.App_Package_Id);
        appInstanceInfo.setMecHost(AppoConstantsTest.Mec_Host);
        appInstanceInfo.setTenant(AppoConstantsTest.Tenant);
        appInstanceInfo.setOperationalStatus("operationalStatus");
        appInstanceInfo.setOperationInfo("operationalInfo");
    }

    @Test
    public void testAppInstanceInfoDto() {
        Assert.assertEquals(AppoConstantsTest.App_Descriptor, appInstanceInfo.getAppDescriptor());
        Assert.assertEquals(AppoConstantsTest.APP_ID, appInstanceInfo.getAppdId());
        Assert.assertEquals(AppoConstantsTest.App_Instance_Id, appInstanceInfo.getAppInstanceId());
        Assert.assertEquals(AppoConstantsTest.App_Name, appInstanceInfo.getAppName());
        Assert.assertEquals(AppoConstantsTest.Applcm_LCM_HOST, appInstanceInfo.getApplcmHost());
        Assert.assertEquals(AppoConstantsTest.App_Package_Id, appInstanceInfo.getAppPackageId());
        Assert.assertEquals(AppoConstantsTest.Mec_Host, appInstanceInfo.getMecHost());
        Assert.assertEquals(AppoConstantsTest.Tenant, appInstanceInfo.getTenant());
        Assert.assertEquals(AppoConstantsTest.Operational_Status, appInstanceInfo.getOperationalStatus());
        Assert.assertEquals(AppoConstantsTest.Operational_Info, appInstanceInfo.getOperationInfo());
    }
}
