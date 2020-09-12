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
        appInstanceInfo.setAppDescriptor(AppoConstantsTest.APP_DESCRIPTOR);
        appInstanceInfo.setAppId(AppoConstantsTest.APP_ID);
        appInstanceInfo.setAppInstanceId(AppoConstantsTest.APP_INSTANCE_ID);
        appInstanceInfo.setAppName(AppoConstantsTest.APP_NAME);
        appInstanceInfo.setApplcmHost(AppoConstantsTest.APPLCM_LCM_HOST);
        appInstanceInfo.setAppPackageId(AppoConstantsTest.APP_PACKAGE_ID);
        appInstanceInfo.setMecHost(AppoConstantsTest.MEC_HOST);
        appInstanceInfo.setTenant(AppoConstantsTest.TENANT);
        appInstanceInfo.setOperationalStatus(AppoConstantsTest.OPERATIONAL_STATUS);
        appInstanceInfo.setOperationInfo(AppoConstantsTest.OPERATIONAL_INFO);
    }

    @Test
    public void testAppInstanceInfoDto() {
        Assert.assertEquals(AppoConstantsTest.APP_DESCRIPTOR, appInstanceInfo.getAppDescriptor());
        Assert.assertEquals(AppoConstantsTest.APP_ID, appInstanceInfo.getAppId());
        Assert.assertEquals(AppoConstantsTest.APP_INSTANCE_ID, appInstanceInfo.getAppInstanceId());
        Assert.assertEquals(AppoConstantsTest.APP_NAME, appInstanceInfo.getAppName());
        Assert.assertEquals(AppoConstantsTest.APPLCM_LCM_HOST, appInstanceInfo.getApplcmHost());
        Assert.assertEquals(AppoConstantsTest.APP_PACKAGE_ID, appInstanceInfo.getAppPackageId());
        Assert.assertEquals(AppoConstantsTest.MEC_HOST, appInstanceInfo.getMecHost());
        Assert.assertEquals(AppoConstantsTest.TENANT, appInstanceInfo.getTenant());
        Assert.assertEquals(AppoConstantsTest.OPERATIONAL_STATUS, appInstanceInfo.getOperationalStatus());
        Assert.assertEquals(AppoConstantsTest.OPERATIONAL_INFO, appInstanceInfo.getOperationInfo());
    }
}
