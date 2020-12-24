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
import org.junit.Assert;
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
public class AppoHandlerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private RestTemplate restTemplate;

    private static final String TENANT_ID = "12db0288-3c67-4042-a708-a8e4a10c6b31";
    private static final String APP_INSTANCE = "/app_instances/";
    private static final String APPO_TENANT = "/appo/v1/tenants/";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SAMPLE_TOKEN = "SampleToken";

    private String appInstanceId;

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void instantiateTerminateTest() throws Exception {

        // Mocking get MEC host from inventory
        String url1 = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/mechosts/1.1.1"
                + ".1";
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(url1))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"1.1.1.1\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"1.1.1.1\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"1.1.1.1\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get applcm from inventory
        String url2 = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/applcms/1.1.1"
                + ".1";
        server.expect(requestTo(url2))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"applcmIp\":\"1.1.1.1\",\"applcmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // applcm port ,

        // Mocking download package from APM
        File file = ResourceUtils.getFile("classpath:22406fba-fd5d-4f55-b3fa-89a45fee913a.csar");
        InputStream ins = new BufferedInputStream(new FileInputStream(file.getPath()));
        InputStreamResource inputStreamResource = new InputStreamResource(ins);
        String url3 = "http://10.9.9.2:11112/apm/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/packages"
                + "/f20358433cf8eb4719a62a49ed118c9b/download";
        server.expect(requestTo(url3))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(inputStreamResource, MediaType.APPLICATION_OCTET_STREAM));

        // Mocking get applcm API
        String url4 = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "1.1.1.1" + "/apps";
        server.expect(requestTo(url4))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        // Create a app instance
        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post(APPO_TENANT + TENANT_ID + "/app_instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(
                                "{ \"appPackageId\": \"f20358433cf8eb4719a62a49ed118c9b\", \"appName\": \"face_recognition\", "
                                        + "\"appId\": \"f50358433cf8eb4719a62a49ed118c9b\", "
                                        + "\"appInstanceDescription\": \"face_recognition\", "
                                        + "\"mecHost\": \"1.1.1.1\" }")
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
        ResultActions getAllResult =
                mvc.perform(MockMvcRequestBuilders.get(APPO_TENANT + TENANT_ID
                        + "/app_instance_infos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE));
        MvcResult getAllMvcResult = getAllResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String getAllResponse = getAllMvcResult.getResponse().getContentAsString();
        Assert.assertEquals("{\"response\":[{\"appInstanceId\":\"test-required\",\"appPackageId\":\"b1bb0ce7-ebca-4fa7-95ed-4840d70a1177\",\"appName\":\"face-recognize\",\"appId\":\"ea8ebc1a-db88-11ea-87d0-0242ac130003\",\"appDescriptor\":\"aaa\",\"mecHost\":\"1.1.1.1\",\"applcmHost\":\"1.1.1.1\",\"operationalStatus\":\"Instantiated\",\"operationInfo\":\"success\"},"
                        + "{\"appInstanceId\":\"" + appInstanceId + "\","
                        + "\"appPackageId\":\"f20358433cf8eb4719a62a49ed118c9b\",\"appName\":\"face_recognition\",\"appId\":\"f50358433cf8eb4719a62a49ed118c9b\",\"appDescriptor\":\"face_recognition\",\"mecHost\":\"1.1.1.1\",\"applcmHost\":\"1.1.1.1\",\"operationalStatus\":\"Created\",\"operationInfo\":\"success\"}]}",
                getAllResponse);

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
                        + "\"appPackageId\":\"f20358433cf8eb4719a62a49ed118c9b\",\"appName\":\"face_recognition\",\"appId\":\"f50358433cf8eb4719a62a49ed118c9b\",\"appDescriptor\":\"face_recognition\",\"mecHost\":\"1.1.1.1\",\"applcmHost\":\"1.1.1.1\",\"operationalStatus\":\"Created\",\"operationInfo\":\"success\"}}",
                getResponse);
        /***********************************************************************************************/

        // Mocking get MEC host from inventory
        String url6 = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/mechosts/1.1.1"
                + ".1";
        server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(url6))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"1.1.1.1\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"1.1.1.1\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"1.1.1.1\",\"appRuleIp\":\"1.1.1.1\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get applcm from inventory
        String url7 = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/applcms/1.1.1"
                + ".1";
        server.expect(requestTo(url7))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"applcmIp\":\"1.1.1.1\",\"applcmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // applcm port ,

        // Mocking get applcm API
        String url8 = "http://1.1.1.1:10000/lcmcontroller/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/instantiate";
        server.expect(requestTo(url8))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        String url9 = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "1.1.1.1" + "/apps/" + appInstanceId;
        server.expect(requestTo(url9))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        // Mocking get applcm API
        String url10 = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "1.1.1.1" + "/apps/" + appInstanceId;
        server.expect(requestTo(url10))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

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

        /***********************************************************************************************/

        // Mocking get MEC host from inventory
        String url11 = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/mechosts/1.1.1"
                + ".1";
        server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(url11))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mechostIp\":\"1.1.1.1\",\"mechostName\":\"TestHost\","
                                + "\"zipCode\":null,"
                                + "\"city\":\"TestCity\","
                                + "\"address\":\"Test Address\",\"affinity\":\"part1,part2\",\"userName\":null,\"edgerepoName\":null,"
                                + "\"edgerepoIp\":\"1.1.1.1\",\"edgerepoPort\":\"10000\",\"edgerepoUsername\":null,"
                                + "\"applcmIp\":\"1.1.1.1\",\"appRuleIp\":\"1.1.1.1\"}",
                        MediaType.APPLICATION_JSON)); // host response , json response, applcm ip ... use applcm url

        // Mocking get applcm from inventory
        String url12 = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/applcms/1.1.1"
                + ".1";
        server.expect(requestTo(url12))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"applcmIp\":\"1.1.1.1\",\"applcmPort\":\"10000\",\"userName\":\"Test\"}",
                        MediaType.APPLICATION_JSON)); /// validate response , use this query , // applcm port ,

        // Mocking get applcm from inventory
        String url13 = "http://10.9.9.1:11111/inventory/v1/tenants/12db0288-3c67-4042-a708-a8e4a10c6b31/app_instances/"+ appInstanceId + "/appd_configuration";
        server.expect(requestTo(url13))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)); /// validate response , use this query , // applcm port ,

        // Mocking get applcm API
        String url14 = "http://1.1.1.1:10000/lcmcontroller/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                appInstanceId + "/terminate";
        server.expect(requestTo(url14))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        String url15 = "http://10.9.9.1:11111/inventory/v1/tenants/" + TENANT_ID + "/mechosts/" +
                "1.1.1.1" + "/apps/" + appInstanceId;
        server.expect(requestTo(url15))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

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

        File deleteDir = new File("src/test/resources/packages/" + appInstanceId);
        FileSystemUtils.deleteRecursively(deleteDir);
    }
}