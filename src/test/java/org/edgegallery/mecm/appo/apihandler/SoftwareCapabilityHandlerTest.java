/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.mecm.appo.apihandler;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppoApplicationTest.class)
@AutoConfigureMockMvc
public class SoftwareCapabilityHandlerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private RestTemplate restTemplate;

    private static final String TENANT_ID = "12db0288-3c67-4042-a708-a8e4a10c6b31";
    private static final String APP_INSTANCE = "/app_instances/";
    private static final String APPO_TENANT = "/appo/v1/tenants/";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SAMPLE_TOKEN = "SampleToken";

    MockRestServiceServer server;

    String inventoryQuery = "http://10.9.9.1:11111/inventory/v1/mechosts/1.1.1.1";

    String mepmQuery = "http://10.9.9.1:11111/inventory/v1/mepms/1.1.1.1";

    String getMepCapablities = "http://1.1.1.1:10000/lcmcontroller/v2/tenants/" + TENANT_ID + "/hosts/" + "1.1.1.1/" +
            "mep_capabilities";

    String getMepCapablity = "http://1.1.1.1:10000/lcmcontroller/v2/tenants/" + TENANT_ID + "/hosts/" + "1.1.1.1/" +
            "mep_capabilities/12";

    @Before
    public void setUp() {

        server = MockRestServiceServer.createServer(restTemplate);

        server.expect(requestTo(inventoryQuery))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"1.1.1.1\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"1.1.1.1\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"mepmIp\":\"1.1.1.1\"}",
                        MediaType.APPLICATION_JSON));

        server.expect(requestTo(mepmQuery))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mepmIp\":\"1.1.1.1\",\"mepmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON));

    }

    @After
    public void shutDown() {
        server.reset();
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void edgeSoftwareHostCapabilitiesTest() throws Exception {
        // Test edgehost capablitlities
        server.expect(requestTo(getMepCapablities))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"[capabilityId\":\"16384563dca094183778a41ea7701d15\","
                        + "\"capabilityName\":\"FaceRegService\",\"status\":\"Active\",\"version\": \"4.5.8\","
                        + "\"consumers\":[{\"applicationInstanceId\":\"5abe4782-2c70-4e47-9a4e-0ee3a1a0fd1f\"},"
                        + "{\"applicationInstanceId\":\"f05a5591-d8f2-4f89-8c0b-8cea6d45712e\"},"
                        + "{\"applicationInstanceId\":\"86dfc97d-325e-4feb-ac4f-280a0ba42513\"}]}",
                        MediaType.APPLICATION_JSON));

        ResultActions edgehostCapabilities =
                mvc.perform(MockMvcRequestBuilders
                        .get(APPO_TENANT + TENANT_ID + "/hosts" + "/1.1.1.1" + "/mep_capabilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult getEdgeHost = edgehostCapabilities.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String edgeResponse = getEdgeHost.getResponse().getContentAsString();
        Assert.assertEquals("{\"[capabilityId\":\"16384563dca094183778a41ea7701d15\",\"capabilityName\":"
                + "\"FaceRegService\",\"status\":\"Active\",\"version\": \"4.5.8\",\"consumers\":"
                + "[{\"applicationInstanceId\":\"5abe4782-2c70-4e47-9a4e-0ee3a1a0fd1f\"},{\"applicationInstanceId\":"
                + "\"f05a5591-d8f2-4f89-8c0b-8cea6d45712e\"},{\"applicationInstanceId\":"
                + "\"86dfc97d-325e-4feb-ac4f-280a0ba42513\"}]}",
                edgeResponse);
        Thread.sleep(5000);
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void edgeHostSoftwareCapabilityTest() throws Exception {
        // Test query edge host capablities based on specific id
        server.expect(requestTo(getMepCapablity))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"capabilityId\":\"16384563dca094183778a41ea7701d15\","
                                + "\"capabilityName\":\"FaceRegService\",\"status\":\"Active\",\"version\": \"4.5.8\","
                                + "\"consumers\":[{\"applicationInstanceId\":\"5abe4782-2c70-4e47-9a4e-0ee3a1a0fd1f\"},"
                                + "{\"applicationInstanceId\":\"f05a5591-d8f2-4f89-8c0b-8cea6d45712e\"},"
                                + "{\"applicationInstanceId\":\"86dfc97d-325e-4feb-ac4f-280a0ba42513\"}}",
                        MediaType.APPLICATION_JSON));

        ResultActions queryEdgehostCapabilities =
                mvc.perform(MockMvcRequestBuilders
                        .get(APPO_TENANT + TENANT_ID + "/hosts" + "/1.1.1.1" + "/mep_capabilities" + "/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult getEdgeHostResult = queryEdgehostCapabilities.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String getEdgeResponse = getEdgeHostResult.getResponse().getContentAsString();
        Assert.assertEquals(
            "{\"capabilityId\":\"16384563dca094183778a41ea7701d15\",\"capabilityName\":\"FaceRegService\","
                + "\"status\":\"Active\",\"version\": \"4.5.8\",\"consumers\":[{\"applicationInstanceId\":"
                + "\"5abe4782-2c70-4e47-9a4e-0ee3a1a0fd1f\"},{\"applicationInstanceId\":"
                + "\"f05a5591-d8f2-4f89-8c0b-8cea6d45712e\"},{\"applicationInstanceId\":"
                + "\"86dfc97d-325e-4feb-ac4f-280a0ba42513\"}}",
            getEdgeResponse);
        Thread.sleep(5000);
    }
}