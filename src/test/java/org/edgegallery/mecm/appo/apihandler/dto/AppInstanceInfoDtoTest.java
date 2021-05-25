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

package org.edgegallery.mecm.appo.apihandler.dto;

import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class AppInstanceInfoDtoTest {

    @InjectMocks
    AppInstanceInfoDto appInstanceInfoDto = new AppInstanceInfoDto();

    @Before
    public void setUp() {
        appInstanceInfoDto.setAppId(AppoConstantsTest.APP_ID);
        appInstanceInfoDto.setAppDescriptor(AppoConstantsTest.APP_DESCRIPTOR);
        appInstanceInfoDto.setAppInstanceId(AppoConstantsTest.APP_INSTANCE_ID);
        appInstanceInfoDto.setMepmHost(AppoConstantsTest.APPLCM_MEPM_HOST);
        appInstanceInfoDto.setAppName(AppoConstantsTest.APP_NAME);
        appInstanceInfoDto.setAppPackageId(AppoConstantsTest.APP_PACKAGE_ID);
        appInstanceInfoDto.setMecHost(AppoConstantsTest.MEC_HOST);
        appInstanceInfoDto.setOperationalStatus(AppoConstantsTest.OPERATIONAL_STATUS);
        appInstanceInfoDto.setOperationInfo(AppoConstantsTest.OPERATIONAL_INFO);
    }

    @Test
    public void testAppInstanceInfoDto() {
        Assert.assertEquals(AppoConstantsTest.APP_ID, appInstanceInfoDto.getAppId());
        Assert.assertEquals(AppoConstantsTest.APP_DESCRIPTOR, appInstanceInfoDto.getAppDescriptor());
        Assert.assertEquals(AppoConstantsTest.APP_INSTANCE_ID, appInstanceInfoDto.getAppInstanceId());
        Assert.assertEquals(AppoConstantsTest.APPLCM_MEPM_HOST, appInstanceInfoDto.getMepmHost());
        Assert.assertEquals(AppoConstantsTest.APP_PACKAGE_ID, appInstanceInfoDto.getAppPackageId());
        Assert.assertEquals(AppoConstantsTest.APP_NAME, appInstanceInfoDto.getAppName());
        Assert.assertEquals(AppoConstantsTest.MEC_HOST, appInstanceInfoDto.getMecHost());
        Assert.assertEquals(AppoConstantsTest.OPERATIONAL_STATUS, appInstanceInfoDto.getOperationalStatus());
        Assert.assertEquals(AppoConstantsTest.OPERATIONAL_INFO, appInstanceInfoDto.getOperationInfo());
    }
}
