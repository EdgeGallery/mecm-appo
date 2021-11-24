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

import org.edgegallery.mecm.appo.apihandler.AppoApplicationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppoApplicationTest.class)
@AutoConfigureMockMvc
public class ResourceMgrSecGrpTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrSecGrpTest.class);

    @Autowired
    MockMvc mvc;

    @Autowired
    private RestTemplate restTemplate;

    private static final String TENANT_ID = "12db0288-3c67-4042-a708-a8e4a10c6b31";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SAMPLE_TOKEN = "SampleToken";
    private static final String RESOURCE_MGR = "/appo/v1/tenants/" + TENANT_ID + "/hosts/3.3.3.3";
    private static final String RESOURCE_CTRL_URL = "http://3.3.3.3:10000/rescontroller/v1/tenants/" + TENANT_ID + "/hosts/3.3.3.3";
    private static final String GROUP_ID = "1234";
    private static final String RULE_ID = "5678";

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

        String url = "http://10.9.9.1:11111/inventory/v1/mechosts/3.3.3.3";
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

    private void querySecurityGroups(MockRestServiceServer server){
        String url =  RESOURCE_CTRL_URL  + "/securityGroups";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":\"null\",\"retCode\":\"0\",\"message\":\"query security group" +
                        " successfully.\",\"params\":\"null\"}", MediaType.APPLICATION_JSON));
    }

    private void querySecurityGroupRules(MockRestServiceServer server){
        String url =  RESOURCE_CTRL_URL  + "/securityGroups/" +
                GROUP_ID + "/securityGroupRules";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":\"null\",\"retCode\":\"0\",\"message\":\"query security group" +
                        "rules successfully.\",\"params\":\"null\"}", MediaType.APPLICATION_JSON));
    }

    private void querySecurityGroupById(MockRestServiceServer server){
        String url =  RESOURCE_CTRL_URL  + "/securityGroups/" +
                GROUP_ID;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":\"null\",\"retCode\":\"0\",\"message\":\"query security group" +
                        "success\",\"params\":\"null\"}", MediaType.APPLICATION_JSON));
    }

    private void deleteSecurityGroup(MockRestServiceServer server){
        String url =  RESOURCE_CTRL_URL  + "/securityGroups/" +
                GROUP_ID;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("{\"data\":\"null\",\"retCode\":\"0\",\"message\":\"delete security " +
                        "group successfully.\",\"params\":\"null\"}", MediaType.APPLICATION_JSON));
    }

    private void deleteSecurityGroupRule(MockRestServiceServer server){
        String url =  RESOURCE_CTRL_URL  + "/securityGroups/" +
                GROUP_ID + "/securityGroupRules/" + RULE_ID;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("{\"data\":\"null\",\"retCode\":\"0\",\"message\":\"delete security" +
                        " group rule successfully.\",\"params\":\"null\"}", MediaType.APPLICATION_JSON));
    }

    private void createSecurityGroup(MockRestServiceServer server){
        String url =  RESOURCE_CTRL_URL  + "/securityGroups";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"data\":\"null\",\"retCode\":\"0\",\"message\":\"create security " +
                        "group successfully.\",\"params\":\"null\"}", MediaType.APPLICATION_JSON));
    }

    private void createSecurityGroupRule(MockRestServiceServer server){
        String url =  RESOURCE_CTRL_URL  + "/securityGroups/" +
                GROUP_ID + "/securityGroupRules";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"data\":\"null\",\"retCode\":\"0\",\"message\":\"create security " +
                        "group rule successfully.\",\"params\":\"null\"}", MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void querySecurityGroups() throws Exception {
        inventoryInfos(server);
        querySecurityGroups(server);
        ResultActions getResult =
                mvc.perform(MockMvcRequestBuilders.get(RESOURCE_MGR + "/securityGroups")
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
    public void querySecurityGroupById() throws Exception {
        inventoryInfos(server);
        querySecurityGroupById(server);
        ResultActions getResult =
                mvc.perform(MockMvcRequestBuilders.get(RESOURCE_MGR + "/securityGroups/" + GROUP_ID)
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
    public void querySecurityGroupRules() throws Exception {
        inventoryInfos(server);
        querySecurityGroupRules(server);
        ResultActions getResult =
                mvc.perform(MockMvcRequestBuilders.get(RESOURCE_MGR + "/securityGroups/" + GROUP_ID
                        + "/securityGroupRules")
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
    public void createSecurityGroup() throws Exception {
        inventoryInfos(server);
        createSecurityGroup(server);
        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post(RESOURCE_MGR + "/securityGroups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .content("{ \"name\": \"new-webservers\"}")
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
    public void createSecurityGroupRule() throws Exception {

        inventoryInfos(server);
        createSecurityGroupRule(server);

        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post(RESOURCE_MGR + "/securityGroups/" + GROUP_ID
                        + "/securityGroupRules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .content("{ \"securityGroupId\": \"a7734e61-b545-452d-a3cd-0189cbd9747a\","
                                + "\"direction\": \"ingress\", "
                                + "\"protocol\": \"tcp\", "
                                + "\"port_range_min\": \"80\", "
                                + "\"port_range_max\": \"90\", "
                                + "\"remoteIpPrefix\": \"test\", "
                                + "\"remote_group_id\": \"85cc3048-abc3-43cc-89b3-377341426ac5\", "
                                + "\"ethertype\": \"IPv4\"}")
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
    public void deleteSecurityGroup() throws Exception {
        inventoryInfos(server);
        deleteSecurityGroup(server);
        ResultActions deleteResult =
                mvc.perform(MockMvcRequestBuilders.delete(RESOURCE_MGR + "/securityGroups/" + GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult deleteMvcResult = deleteResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String deleteResponse = deleteMvcResult.getResponse().getContentAsString();
        LOGGER.info("response deleted securityGroupId: {}",deleteResponse);
        assertNotNull(deleteResponse);
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void deleteSecurityGroupRule() throws Exception {
        inventoryInfos(server);
        deleteSecurityGroupRule(server);
        ResultActions deleteResult =
                mvc.perform(MockMvcRequestBuilders.delete(RESOURCE_MGR + "/securityGroups/" + GROUP_ID
                        + "/securityGroupRules/" + RULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult deleteMvcResult = deleteResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String deleteResponse = deleteMvcResult.getResponse().getContentAsString();
        LOGGER.info("response deleted security group rule ID: {}",deleteResponse);
        assertNotNull(deleteResponse);
    }

}