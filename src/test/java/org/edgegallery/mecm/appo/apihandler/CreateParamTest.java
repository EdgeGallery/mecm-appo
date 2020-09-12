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

package org.edgegallery.mecm.appo.apihandler;

import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class CreateParamTest {

    @InjectMocks
    CreateParam createParam = new CreateParam();

    @Before
    public void setUp() {
        createParam.setAppId(AppoConstantsTest.APP_ID);
    }

    @Test
    public void testCreateParam() {
        Assert.assertEquals(AppoConstantsTest.APP_ID, createParam.getAppId());
    }

}
