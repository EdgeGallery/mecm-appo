/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.mecm.appo.apihandler.resmgr;

import java.io.File;

import org.edgegallery.mecm.appo.apihandler.AppoApplicationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppoApplicationTest.class)
@AutoConfigureMockMvc
public class ResourceMgrFlavorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrFlavorTest.class);

    @Autowired
    MockMvc mvc;

    @Autowired
    private RestTemplate restTemplate;

    private static final String TENANT_ID = "12db0288-3c67-4042-a708-a8e4a10c6b31";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SAMPLE_TOKEN = "SampleToken";
    private static final String RESOURCE_MGR = "/appo/v1/tenants/" + TENANT_ID +"/hosts/3.3.3.3";
    private static final String RESOURCE_CTRL_URL = "http://3.3.3.3:10000/rescontroller/v1/tenants/" + TENANT_ID +
            "/hosts/3.3.3.3";
    private static final String FLAVOR_ID = "1234";

    private File deleteDir;
    private MockRestServiceServer server;

    @Before
    public void setUp() {
        server = MockRestServiceServer.createServer(restTemplate);
    }

    private MockRestServiceServer resetServer(MockRestServiceServer server) {
        server.reset();
        server = MockRestServiceServer.createServer(restTemplate);
        return server;
    }

    @After
    public void clear() {
        FileSystemUtils.deleteRecursively(deleteDir);
        server.reset();
    }

    private void inventoryInfos(MockRestServiceServer server){

        String url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/3.3.3.3";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"3.3.3.3\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"3.3.3.3\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"mepmIp\":\"3.3.3.3\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, mepm ip ... use mepm url

        // Mocking get mepm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/mepms/3.3.3.3";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mepmIp\":\"3.3.3.3\",\"mepmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON));

    }

    private void flavorInfos(MockRestServiceServer server){

        // Mocking get mepm API
        String url =  RESOURCE_CTRL_URL  + "/flavors";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n\"data\": null,\n\"retCode\": 0,\n\"message\": \"query flavor " +
                        "success\",\n\"params\": null\n}", MediaType.APPLICATION_JSON));
    }

    private void createFlavorInfos(MockRestServiceServer server){

        // Mocking post mepm API
        String url =  RESOURCE_CTRL_URL  + "/flavors";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n\"data\": null,\n\"retCode\": 0,\n\"message\": \"create flavor " +
                        "success\",\n\"params\": null\n}", MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void createFlavors() throws Exception {

        inventoryInfos(server);
        createFlavorInfos(server);

        //create flavour
        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post(RESOURCE_MGR + "/flavors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .content(
                                "{ \"name\": \"test_flavor\", \"vcpus\": \"2\", "
                                        + "\"ram\": \"1024\", "
                                        + "\"disk\": \"10\", "
                                        + "\"swap\": \"10\","
                                        + "\"extraSpecs\": { \"EG\": \"true\"}}")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult postMvcResult = postResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String postResponse = postMvcResult.getResponse().getContentAsString();
        LOGGER.info("response: {}",postResponse);
        assertNotNull(postResponse);
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void queryFlavors() throws Exception {

        inventoryInfos(server);
        flavorInfos(server);

        // Create a queryFlavors
        ResultActions getResult =
                mvc.perform(MockMvcRequestBuilders.get(RESOURCE_MGR + "/flavors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult getMvcResult = getResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String postResponse = getMvcResult.getResponse().getContentAsString();
        assertNotNull(postResponse);
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void queryFlavorsById() throws Exception {

        inventoryInfos(server);

        // Mocking get mepm API
        String url1 =  RESOURCE_CTRL_URL  + "/flavors/" + FLAVOR_ID;
        server.expect(requestTo(url1))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n\"data\": null,\n\"retCode\": 0,\n\"message\": \"queryById flavor " +
                        "success\",\n\"params\": null\n}", MediaType.APPLICATION_JSON));

        // Create a queryFlavors By Id
        ResultActions getResult =
                mvc.perform(MockMvcRequestBuilders.get(RESOURCE_MGR + "/flavors/" + FLAVOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult getMvcResult = getResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String postResponse = getMvcResult.getResponse().getContentAsString();
        LOGGER.info("response flavorId: {}",postResponse);
        assertNotNull(postResponse);
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void deleteFlavorsById() throws Exception {
        inventoryInfos(server);

        // Mocking get mepm API
        String url1 =  RESOURCE_CTRL_URL  + "/flavors/" + FLAVOR_ID;
        server.expect(requestTo(url1))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("{\n\"data\": null,\n\"retCode\": 0,\n\"message\": \"delete flavor " +
                        "success\",\n\"params\": null\n}", MediaType.APPLICATION_JSON));

        // Create a queryFlavors By Id
        ResultActions deleteResult =
                mvc.perform(MockMvcRequestBuilders.delete(RESOURCE_MGR + "/flavors/" + FLAVOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult deleteMvcResult = deleteResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
       String deleteResponse = deleteMvcResult.getResponse().getContentAsString();
       LOGGER.info("response deleted flavorId: {}",deleteResponse);
       assertNotNull(deleteResponse);
    }

}