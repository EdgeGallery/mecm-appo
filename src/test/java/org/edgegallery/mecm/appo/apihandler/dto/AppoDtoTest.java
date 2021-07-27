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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppoDtoTest {

    @InjectMocks
    DstInterfaceDto interfaceDto = new DstInterfaceDto();
    TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();
    AppInstanceDeletedDto appInstanceDeletedDto = new AppInstanceDeletedDto();
    SyncDeletedAppInstanceDto appInstanceDto = new SyncDeletedAppInstanceDto();
    BatchResponseDto responseDto = new BatchResponseDto();
    AppInstantiateReqParam reqParam = new AppInstantiateReqParam();
    AppRuleDeleteConfigDto deleteConfigDto = new AppRuleDeleteConfigDto();
    AppRuleConfigDto appRuleConfigDto = new AppRuleConfigDto();
    AppRuleTaskDto appRuleTaskDto = new AppRuleTaskDto();
    BatchCreateParam batchCreateParam = new BatchCreateParam();
    BatchInstancesParam instancesParam = new BatchInstancesParam();
    BatchInstancesReqParam batchReqParam = new BatchInstancesReqParam();
    BatchTerminateReqParam terminateReqParam = new BatchTerminateReqParam();
    CreateParam createParam = new CreateParam();
    DnsRuleDto dnsRuleDto = new DnsRuleDto();
    TrafficFilterDto filterDto = new TrafficFilterDto();
    TrafficRuleDto trafficRuleDto = new TrafficRuleDto();

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
        List<AppInstanceDeletedDto> deletedDtos = new ArrayList<>();
        deletedDtos.add(appInstanceDeletedDto);
        appInstanceDto.setAppInstanceDeletedRecs(deletedDtos);
        responseDto.setHost("1.1.1.1");
        Map<String, String> map = new HashMap<>();
        map.put("param", "reqParam");
        reqParam.setParameters(map);
        Set<String> set = new HashSet<>();
        set.add("appDNSRule");
        List<String> list = new ArrayList<>();
        list.add("mecHost");
        deleteConfigDto.setAppDNSRule(set);
        appRuleConfigDto.setAppSupportMp1(true);
        appRuleTaskDto.setTaskId("taskId");
        batchCreateParam.setMecHost(list);
        List<BatchInstancesParam> batchList = new ArrayList<>();
        instancesParam.setAppInstanceId("instance_id");
        batchList.add(instancesParam);
        batchReqParam.setInstantiationParameters(batchList);
        terminateReqParam.setAppInstanceIds(list);
        createParam.setMecHost("mechost");
        dnsRuleDto.setTtl(1);
        filterDto.setDSCP(2);
        trafficRuleDto.setPriority(5);

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
        Assert.assertNotNull(appInstanceDto.getAppInstanceDeletedRecs());
        Assert.assertNotNull(appInstanceDto.toString());
        Assert.assertEquals("1.1.1.1", responseDto.getHost());
        Assert.assertNotNull(responseDto.toString());
        Assert.assertNotNull(reqParam.toString());
        Assert.assertNotNull(deleteConfigDto.toString());
        Assert.assertNotNull(appRuleConfigDto.toString());
        Assert.assertNotNull(appRuleTaskDto.toString());
        Assert.assertNotNull(batchCreateParam.toString());
        Assert.assertNotNull(instancesParam.toString());
        Assert.assertNotNull(batchReqParam.toString());
        Assert.assertNotNull(terminateReqParam.toString());
        Assert.assertNotNull(createParam.toString());
        Assert.assertNotNull(dnsRuleDto.toString());
        Assert.assertNotNull(filterDto.toString());
        Assert.assertNotNull(trafficRuleDto.toString());

    }
}
