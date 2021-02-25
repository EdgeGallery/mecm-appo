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
public class AppRulesConfigurationTest {

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
        String url = "http://10.9.9.1:11111/inventory/v1/mechosts/2.2.2"
                + ".2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"2.2.2.2\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"2.2.2.2\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"2.2.2.2\", \"appRuleIp\":\"2.2.2.2\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get applcm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/applcms/2.2.2"
                + ".2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"applcmIp\":\"2.2.2.2\",\"applcmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // applcm port ,

        // Mocking download package from APM
        File file = ResourceUtils.getFile("classpath:22406fba-fd5d-4f55-b3fa-89a45fee913a-apprules.csar");
        InputStream ins = new BufferedInputStream(new FileInputStream(file.getPath()));
        InputStreamResource inputStreamResource = new InputStreamResource(ins);
        url = "http://10.9.9.2:11112/apm/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/packages"
                + "/f20358433cf8eb4719a62a49ed118c9b/download";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(inputStreamResource, MediaType.APPLICATION_OCTET_STREAM));

        // Mocking get apprule from inventory
        url = "http://10.9.9.1:11111/inventory/v1/apprulemanagers/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"appRuleIp\":\"2.2.2.2\",\"appRulePort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "2.2.2.2" + "/apps";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
    }

    private void instantiateAppInstanceFlowUrls(MockRestServiceServer server, String appInstanceId) {
        // Mocking get MEC host from inventory
        String url = "http://10.9.9.1:11111/inventory/v1/mechosts/2.2.2"
                + ".2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"2.2.2.2\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"2.2.2.2\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"2.2.2.2\",\"appRuleIp\":\"2.2.2.2\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get applcm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/applcms/2.2.2"
                + ".2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"applcmIp\":\"2.2.2.2\",\"applcmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // applcm port ,

        // Mocking get apprule from inventory
        url = "http://10.9.9.1:11111/inventory/v1/apprulemanagers"
                + "/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"appRuleIp\":\"2.2.2.2\",\"appRulePort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://2.2.2.2:10000/lcmcontroller/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/instantiate";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        url = "http://10.9.9.1:11111/inventory/v1/mechosts/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"2.2.2.2\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"2.2.2.2\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"2.2.2.2\",\"appRuleIp\":\"2.2.2.2\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get apprule from inventory
        url = "http://10.9.9.1:11111/inventory/v1/apprulemanagers/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"appRuleIp\":\"2.2.2.2\",\"appRulePort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/app_instances/"
                + appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)); /// validate response , use this query , // applcm port ,

        // Mocking get applcm API
        url = "http://2.2.2.2:10000/apprulemgr/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"taskId\": \"\",\"appInstanceId\": \"40519ee1-fb9d-4a61-855c-1b5c2a41da9f\","
                                + "\"detailed\": \"duplicate dns entry\",\"configResult\": \"FAILURE\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "2.2.2.2" + "/apps/" + appInstanceId;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"appInstanceId\":\"7c948939-983c-4a3a-bd68-2bdff2a86ecd\","
                        + "\"appName\":\"face_recognitionRule\",\"packageId\":\"f20358433cf8eb4719a62a49ed118c9b\","
                        + "\"status\":\"Created\"}", MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "2.2.2.2" + "/apps/" + appInstanceId;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());
    }

    private void deleteAppInstanceFlowUrls(MockRestServiceServer server, String appInstanceId) {
        // Mocking get MEC host from inventory
        String url = "http://10.9.9.1:11111/inventory/v1/mechosts/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"2.2.2.2\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"2.2.2.2\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"2.2.2.2\",\"appRuleIp\":\"2.2.2.2\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get applcm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/applcms/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"applcmIp\":\"2.2.2.2\",\"applcmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // applcm port ,

        // Mocking get applcm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/app_instances"
                + "/" + appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"applcmIp\":\"2.2.2.2\",\"applcmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // applcm port ,

        // Mocking get apprule from inventory
        url = "http://10.9.9.1:11111/inventory/v1/apprulemanagers"
                + "/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"appRuleIp\":\"2.2.2.2\",\"appRulePort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://2.2.2.2:10000/apprulemgr/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("{\"taskId\": \"\",\"appInstanceId\": \"40519ee1-fb9d-4a61-855c-1b5c2a41da9f\","
                                + "\"detailed\": \"duplicate dns entry\",\"configResult\": \"FAILURE\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm from inventory
        url = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/app_instances"
                + "/" + appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK)); /// validate response , use this query , // applcm port ,

        // Mocking get applcm API
        url = "http://2.2.2.2:10000/lcmcontroller/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/terminate";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "2.2.2.2" + "/apps/" + appInstanceId;
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
    }

    private void appRulesUpdateFlowUrls(MockRestServiceServer server, String appInstanceId) {
        // Mocking get MEC host from inventory
        String url = "http://10.9.9.1:11111/inventory/v1/mechosts/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"2.2.2.2\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"2.2.2.2\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"2.2.2.2\",\"appRuleIp\":\"2.2.2.2\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get apprule from inventory
        url = "http://10.9.9.1:11111/inventory/v1/apprulemanagers"
                + "/2.2.2.2";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"appRuleIp\":\"2.2.2.2\",\"appRulePort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm from inventory
        /*url = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/app_instances/"
                + appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));*/

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n"
                                + "  \"appTrafficRule\": [\n"
                                + "    {\n"
                                + "      \"trafficRuleId\": \"trafficRule1\",\n"
                                + "      \"filterType\": \"FLOW\",\n"
                                + "      \"priority\": 1,\n"
                                + "      \"action\": \"PASSTHROUGH\",\n"
                                + "      \"trafficFilter\": [\n"
                                + "        {\n"
                                + "          \"srcAddress\": [\n"
                                + "            \"0.0.0.0/0\"\n"
                                + "          ],\n"
                                + "          \"srcPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"dstAddress\": [\n"
                                + "            \"172.30.2.0/28\"\n"
                                + "          ],\n"
                                + "          \"dstPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"protocol\": [\n"
                                + "            \"ANY\"\n"
                                + "          ],\n"
                                + "          \"tag\": [\n"
                                + "            \"1234\"\n"
                                + "          ],\n"
                                + "          \"srcTunnelAddress\": [\n"
                                + "            \"10.10.10.10\"\n"
                                + "          ],\n"
                                + "          \"srcTunnelPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"dstTunnelPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"qCI\": 1,\n"
                                + "          \"dSCP\": 0,\n"
                                + "          \"tC\": 1\n"
                                + "        }\n"
                                + "      ]\n"
                                + "    }\n"
                                + "  ],\n"
                                + "  \"appDNSRule\": [\n"
                                + "    {\n"
                                + "      \"dnsRuleId\": \"dnsRule1\",\n"
                                + "      \"domainName\": \"positioningservice.org\",\n"
                                + "      \"ipAddressType\": \"IP_V4\",\n"
                                + "      \"ipAddress\": \"172.30.2.17\",\n"
                                + "      \"ttl\": 100\n"
                                + "    }\n"
                                + "  ],\n"
                                + "  \"appSupportMp1\": true,\n"
                                + "  \"appName\": \"face_recognitionRule\"\n"
                                + "}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://2.2.2.2:10000/apprulemgr/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"taskId\": \"\",\"appInstanceId\": \"40519ee1-fb9d-4a61-855c-1b5c2a41da9f\","
                                + "\"detailed\": \"duplicate dns entry\",\"configResult\": \"SUCCESS\"}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n"
                                + "  \"appTrafficRule\": [\n"
                                + "    {\n"
                                + "      \"trafficRuleId\": \"TrafficRule1\",\n"
                                + "      \"filterType\": \"FLOW\",\n"
                                + "      \"priority\": 1,\n"
                                + "      \"trafficFilter\": [\n"
                                + "        {\n"
                                + "          \"srcAddress\": [\n"
                                + "            \"192.168.1.1/28\"\n"
                                + "          ],\n"
                                + "          \"dstAddress\": [\n"
                                + "            \"192.168.1.1/28\"\n"
                                + "          ],\n"
                                + "          \"srcPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"dstPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"protocol\": [\n"
                                + "            \"TCP\"\n"
                                + "          ],\n"
                                + "          \"qCI\": 1,\n"
                                + "          \"dSCP\": 0,\n"
                                + "          \"tC\": 1\n"
                                + "        }\n"
                                + "      ],\n"
                                + "      \"action\": \"DROP\",\n"
                                + "      \"state\": \"ACTIVE\"\n"
                                + "    }\n"
                                + "  ],\n"
                                + "  \"appDNSRule\": [\n"
                                + "    {\n"
                                + "      \"dnsRuleId\": \"dnsRule1\",\n"
                                + "      \"domainName\": \"www.example.com\",\n"
                                + "      \"ipAddressType\": \"IP_V4\",\n"
                                + "      \"ipAddress\": \"192.0.2.0\",\n"
                                + "      \"ttl\": 30,\n"
                                + "      \"state\": \"ACTIVE\"\n"
                                + "    }\n"
                                + "  ],\n"
                                + "  \"appSupportMp1\": true,\n"
                                + "  \"appName\": \"face_recognitionRule\"\n"
                                + "}",
                        MediaType.APPLICATION_JSON));

        // Mocking get applcm API
        url = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/appd_configuration";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());
    }

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void instantiateTerminateTest() throws Exception {
        String appInstanceId;

        /*****Execute create app instance*****/
        createAppInstanceFlowUrls(server);

        // Create a app instance
        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post(APPO_TENANT + TENANT_ID + "/app_instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(
                                "{ \"appPackageId\": \"f20358433cf8eb4719a62a49ed118c9b\", \"appName\": "
                                        + "\"face_recognitionRule\", "
                                        + "\"appId\": \"f50358433cf8eb4719a62a49ed118c9b\", "
                                        + "\"appInstanceDescription\": \"face_recognitionRule\", "
                                        + "\"mecHost\": \"2.2.2.2\" }")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        MvcResult postMvcResult = postResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String postResponse = postMvcResult.getResponse().getContentAsString();
        assertThat(postResponse, containsString("app_instance_id"));

        // Sleep for create to finish as its a async call
        Thread.sleep(5000);

        // Test Get all instance id
        // Get application instance id
        appInstanceId = postResponse.substring(32, 68);

        // Get application instance id
        ResultActions getResult =
                mvc.perform(MockMvcRequestBuilders.get(APPO_TENANT + TENANT_ID
                        + "/app_instance_infos/" + appInstanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE));
        MvcResult getMvcResult = getResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String getResponse = getMvcResult.getResponse().getContentAsString();
        Assert.assertEquals("{\"response\":{\"appInstanceId\":\"" + appInstanceId + "\","
                        + "\"appPackageId\":\"f20358433cf8eb4719a62a49ed118c9b\",\"appName\":\"face_recognitionRule\","
                        + "\"appId\":\"f50358433cf8eb4719a62a49ed118c9b\",\"appDescriptor\":\"face_recognitionRule\",\"mecHost\":\"2.2.2.2\",\"applcmHost\":\"2.2.2.2\",\"operationalStatus\":\"Created\",\"operationInfo\":\"success\"}}",
                getResponse);

        /*****Execute Instantiate app instance*****/
        instantiateAppInstanceFlowUrls(resetServer(server), appInstanceId);

        // Test instantiate
        ResultActions postInstantiateResult =
                mvc.perform(MockMvcRequestBuilders
                        .post(APPO_TENANT + TENANT_ID + APP_INSTANCE + appInstanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        postInstantiateResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        Thread.sleep(5000);

        /*****Execute app rule configuration to the app instance*****/
        appRulesUpdateFlowUrls(resetServer(server), appInstanceId);
        // Configure app rules
        ResultActions postConfigAppRuleResult =
                mvc.perform(MockMvcRequestBuilders
                        .post(APPO_TENANT + TENANT_ID + APP_INSTANCE + appInstanceId + "/appd_configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\n"
                                + "  \"appTrafficRule\": [\n"
                                + "    {\n"
                                + "      \"trafficRuleId\": \"TrafficRule1\",\n"
                                + "      \"filterType\": \"FLOW\",\n"
                                + "      \"priority\": 1,\n"
                                + "      \"trafficFilter\": [\n"
                                + "        {\n"
                                + "          \"srcAddress\": [\n"
                                + "            \"192.168.1.1/28\"\n"
                                + "          ],\n"
                                + "          \"dstAddress\": [\n"
                                + "            \"192.168.1.1/28\"\n"
                                + "          ],\n"
                                + "          \"srcPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"dstPort\": [\n"
                                + "            \"8080\"\n"
                                + "          ],\n"
                                + "          \"protocol\": [\n"
                                + "            \"TCP\"\n"
                                + "          ],\n"
                                + "          \"qCI\": 1,\n"
                                + "          \"dSCP\": 0,\n"
                                + "          \"tC\": 1\n"
                                + "        }\n"
                                + "      ],\n"
                                + "      \"action\": \"DROP\",\n"
                                + "      \"state\": \"ACTIVE\"\n"
                                + "    }\n"
                                + "  ],\n"
                                + "  \"appDNSRule\": [\n"
                                + "    {\n"
                                + "      \"dnsRuleId\": \"dnsRule1\",\n"
                                + "      \"domainName\": \"www.example.com\",\n"
                                + "      \"ipAddressType\": \"IP_V4\",\n"
                                + "      \"ipAddress\": \"192.0.2.0\",\n"
                                + "      \"ttl\": 30,\n"
                                + "      \"state\": \"ACTIVE\"\n"
                                + "    }\n"
                                + "  ],\n"
                                + "  \"appSupportMp1\": true,\n"
                                + "  \"appName\": \"abc\"\n"
                                + "}")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        postMvcResult = postConfigAppRuleResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        postResponse = postMvcResult.getResponse().getContentAsString();
        assertThat(postResponse, containsString("apprule_task_id"));
        Thread.sleep(5000);

        /*****Execute terminate app instance*****/
        deleteAppInstanceFlowUrls(resetServer(server), appInstanceId);

        // delete
        ResultActions deleteResult =
                mvc.perform(MockMvcRequestBuilders
                        .delete(APPO_TENANT + TENANT_ID + APP_INSTANCE + appInstanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        deleteResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        deleteDir = new File("src/test/resources/packages/" + appInstanceId);
    }
}