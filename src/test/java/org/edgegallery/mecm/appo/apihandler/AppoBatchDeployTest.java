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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppoApplicationTest.class)
@AutoConfigureMockMvc
public class AppoBatchDeployTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private RestTemplate restTemplate;

    private static final String TENANT_ID = "12db0288-3c67-4042-a708-a8e4a10c6b31";
    private static final String APP_INSTANCE = "/app_instances/";
    private static final String APPO_TENANT = "/appo/v1/tenants/";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SAMPLE_TOKEN = "SampleToken";

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

    private void createAppInstanceFlowUrls(MockRestServiceServer server)  throws Exception {
        // Mocking get MEC host from inventory
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
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // mepm port ,

        // Mocking download package from APM
        File file = ResourceUtils.getFile("classpath:22406fba-fd5d-4f55-b3fa-89a45fee913a.csar");
        InputStream ins = new BufferedInputStream(new FileInputStream(file.getPath()));
        InputStreamResource inputStreamResource = new InputStreamResource(ins);
        url = "http://10.9.9.2:11112/apm/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/packages"
                + "/f20358433cf8eb4719a62a49ed118c9b/download";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(inputStreamResource, MediaType.APPLICATION_OCTET_STREAM));

        // Mocking get mepm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "3.3.3.3" + "/apps";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
    }

    private void instantiateAppInstanceFlowUrls(MockRestServiceServer server, String appInstanceId) {
        // Mocking get MEC host from inventory
        String url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/3.3.3"
                + ".3";
        server = MockRestServiceServer.createServer(restTemplate);
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
        url = "http://10.9.9.1:11111/inventory/v1/mepms/3.3.3"
                + ".3";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mepmIp\":\"3.3.3.3\",\"mepmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // mepm port ,

        // Mocking get mepm API
        url = "http://3.3.3.3:10000/lcmcontroller/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/instantiate";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "3.3.3.3" + "/apps/" + appInstanceId;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"appInstanceId\":\"7c948939-983c-4a3a-bd68-2bdff2a86ecd\","
                        + "\"appName\":\"face_recognitionBatch\",\"packageId\":\"f20358433cf8eb4719a62a49ed118c9b\","
                        + "\"status\":\"Created\"}", MediaType.APPLICATION_JSON));

        // Mocking get mepm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "3.3.3.3" + "/apps/" + appInstanceId;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());
    }

    private void deleteAppInstanceFlowUrls(MockRestServiceServer server, String appInstanceId) {
        // Mocking get MEC host from inventory
        String url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/3.3.3.3";
        server = MockRestServiceServer.createServer(restTemplate);
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
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // mepm port ,

        // Mocking get mepm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/app_instances/"
                + appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)); /// validate response , use this query , // mepm port ,

        // Mocking get mepm API
        url = "http://3.3.3.3:10000/lcmcontroller/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/terminate";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "3.3.3.3" + "/apps/" + appInstanceId;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void batchInstantiateTerminateTest() throws Exception {
        String appInstanceId;

        createAppInstanceFlowUrls(server);

        // Create a app instance
        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post(APPO_TENANT + TENANT_ID + "/app_instances/batch_create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .content(
                                "{ \"appPackageId\": \"f20358433cf8eb4719a62a49ed118c9b\", \"appName\": "
                                        + "\"face_recognitionBatch\", "
                                        + "\"appId\": \"f50358433cf8eb4719a62a49ed118c9b\", "
                                        + "\"appInstanceDescription\": \"face_recognition\", "
                                        + "\"mecHost\": [\"3.3.3.3\"] }")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult postMvcResult = postResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String postResponse = postMvcResult.getResponse().getContentAsString();
        assertThat(postResponse, containsString("appInstanceId"));

        // Sleep for create to finish as its a async call
        Thread.sleep(5000);

        // Test Get all instance id
        // Get application instance id
        appInstanceId = postResponse.substring(31, 67);
        ResultActions getAllResult =
                mvc.perform(MockMvcRequestBuilders.get(APPO_TENANT + TENANT_ID
                        + "/app_instance_infos?appinstanceids=" + appInstanceId)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf())
                        .accept(MediaType.APPLICATION_JSON_VALUE));
        MvcResult getAllMvcResult = getAllResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String getAllResponse = getAllMvcResult.getResponse().getContentAsString();
        Assert.assertEquals("{\"response\":[{\"appInstanceId\":\"" + appInstanceId + "\","
                        + "\"appPackageId\":\"f20358433cf8eb4719a62a49ed118c9b\","
                        + "\"appName\":\"face_recognitionBatch\",\"appId\":\"f50358433cf8eb4719a62a49ed118c9b\","
                        + "\"appDescriptor\":\"face_recognition\",\"mecHost\":\"3.3.3.3\",\"mepmHost\":\"3.3.3.3\","
                        + "\"operationalStatus\":\"Created\",\"operationInfo\":\"success\"}]}",
                getAllResponse);

        // Get application instance id
        ResultActions getResult =
                mvc.perform(MockMvcRequestBuilders.get(APPO_TENANT + TENANT_ID
                        + "/app_instance_infos/" + appInstanceId)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf())
                        .accept(MediaType.APPLICATION_JSON_VALUE));
        MvcResult getMvcResult = getResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String getResponse = getMvcResult.getResponse().getContentAsString();
        Assert.assertEquals("{\"response\":{\"appInstanceId\":\"" + appInstanceId + "\","
                        + "\"appPackageId\":\"f20358433cf8eb4719a62a49ed118c9b\","
                        + "\"appName\":\"face_recognitionBatch\",\"appId\":\"f50358433cf8eb4719a62a49ed118c9b\","
                        + "\"appDescriptor\":\"face_recognition\",\"mecHost\":\"3.3.3.3\",\"mepmHost\":\"3.3.3.3\","
                        + "\"operationalStatus\":\"Created\",\"operationInfo\":\"success\"}}",
                getResponse);
        /***********************************************************************************************/
        instantiateAppInstanceFlowUrls(resetServer(server), appInstanceId);

        // Test instantiate
        ResultActions postInstantiateResult =
                mvc.perform(MockMvcRequestBuilders
                        .post(APPO_TENANT + TENANT_ID + APP_INSTANCE + "batch_instantiate")
                        .contentType(MediaType.APPLICATION_JSON).with(csrf())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        //.content("{ \"appInstanceIds\": [\"" + appInstanceId + "\"] }")
                        .content("{\n"
                                + "    \"instantiationParameters\": [\n"
                                + "        {  \n"
                                + "          \"appInstanceId\": \"" + appInstanceId + "\",\n"
                                + "          \"parameters\": {  \"abilityZone\"    : \"zone\",\n"
                                + "                          \"ServerGroup\"    : \"serverGroup\",\n"
                                + "                          \"Net_n6_ip\"      : \"net_n6_i\",\n"
                                + "                          \"Net_n6_mask\"    : \"net_n6_mask\",\n"
                                + "                          \"Net_n6_vlan_id\" : \"net_n6_vlan_id\",\n"
                                + "                          \"Net_n6_phy_name\": \"net_n6_phy_name\",\n"
                                + "                          \"mepIp\"          : \"mep ip\",\n"
                                + "                          \"mepPort\"        : \"mep port\",\n"
                                + "                          \"ak\"             : \"ak\",\n"
                                + "                          \"sk\"             : \"sk\"  \n"
                                + "          }\n"
                                + "        }\n"
                                + "    ]\n"
                                + "}")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        postInstantiateResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        Thread.sleep(5000);

        /***********************************************************************************************/
        deleteAppInstanceFlowUrls(resetServer(server), appInstanceId);

        // delete
        ResultActions deleteResult =
                mvc.perform(MockMvcRequestBuilders
                        .post(APPO_TENANT + TENANT_ID + APP_INSTANCE + "batch_terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).with(csrf())
                        .content("{ \"appInstanceIds\": [\"" + appInstanceId + "\"] }")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        deleteResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        File deleteDir = new File("src/test/resources/packages/" + appInstanceId);
    }

}