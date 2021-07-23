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

public class AppoDtoTest {

    @InjectMocks
    DstInterfaceDto interfaceDto = new DstInterfaceDto();
    TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();

    @Before
    public void setUp() {
        tunnelInfoDto.setTunnelType("tunnelType");
        tunnelInfoDto.setTunnelDstAddress("destAddress");
        tunnelInfoDto.setTunnelSrcAddress("sourceAddress");
        tunnelInfoDto.setTunnelSpecificData("date");

        interfaceDto.setInterfaceType("interfaceType");
        interfaceDto.setDstIpAddress("1.1.1.1");
        interfaceDto.setDstMacAddress("destAddress");
        interfaceDto.setSrcMacAddress("sourceAddress");
        interfaceDto.setTunnelInfo(tunnelInfoDto);
    }

    @Test
    public void testAppInstanceInfoDto() {
        Assert.assertEquals("tunnelType", tunnelInfoDto.getTunnelType());
        Assert.assertEquals("destAddress", tunnelInfoDto.getTunnelDstAddress());
        Assert.assertEquals("sourceAddress", tunnelInfoDto.getTunnelSrcAddress());
        Assert.assertEquals("date", tunnelInfoDto.getTunnelSpecificData());
        Assert.assertNotNull(tunnelInfoDto.toString());
        Assert.assertEquals("interfaceType", interfaceDto.getInterfaceType());
        Assert.assertEquals("1.1.1.1", interfaceDto.getDstIpAddress());
        Assert.assertEquals("destAddress", interfaceDto.getDstMacAddress());
        Assert.assertEquals("sourceAddress", interfaceDto.getSrcMacAddress());
        Assert.assertEquals(tunnelInfoDto, interfaceDto.getTunnelInfo());
        Assert.assertNotNull(interfaceDto.toString());

    }
}
