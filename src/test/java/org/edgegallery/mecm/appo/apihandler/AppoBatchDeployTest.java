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
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppoApplicationTest.class)
@AutoConfigureMockMvc
public class AppoBatchDeployTest {

    private static final String TENANT_ID = "12db0288-3c67-4042-a708-a8e4a10c6b31";
    private static final String APP_INSTANCE = "/app_instances/";
    private static final String APPO_TENANT = "/appo/v1/tenants/";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SAMPLE_TOKEN = "SampleToken";
    @Autowired
    MockMvc mvc;
    @Autowired
    private RestTemplate restTemplate;
    private String appInstanceId;

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void batchDeployInstantiateTerminateTest() throws Exception {

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

        // Mocking get applcm API
        String url5 = "http://1.1.1.1:10000/lcmcontroller/v1/tenants/" + TENANT_ID + APP_INSTANCE +
                "batch_instantiate";
        server.expect(requestTo(url5))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        // Create a app instance
        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post(APPO_TENANT + TENANT_ID + "/app_instances/batch_create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(
                                "{ \"appPackageId\": \"f20358433cf8eb4719a62a49ed118c9b\", \"appName\": \"face_recognition\", "
                                        + "\"appId\": \"f50358433cf8eb4719a62a49ed118c9b\", "
                                        + "\"appInstanceDescription\": \"face_recognition\", "
                                        + "\"mecHost\": [\"1.1.1.1\"] }")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE));
        MvcResult getAllMvcResult = getAllResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String getAllResponse = getAllMvcResult.getResponse().getContentAsString();
        Assert.assertEquals("{\"response\":[{\"appInstanceId\":\"" + appInstanceId + "\","
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

        // Test instantiate
        ResultActions postInstantiateResult =
                mvc.perform(MockMvcRequestBuilders
                        .post(APPO_TENANT + TENANT_ID + APP_INSTANCE + "batch_instantiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content("{ \"appInstanceIds\": [\"" + appInstanceId + "\"] }")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        postInstantiateResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        Thread.sleep(5000);

        // delete
        ResultActions deleteResult =
                mvc.perform(MockMvcRequestBuilders
                        .post(APPO_TENANT + TENANT_ID + APP_INSTANCE + "batch_terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{ \"appInstanceIds\": [\"" + appInstanceId + "\"] }")
                        .header(ACCESS_TOKEN, SAMPLE_TOKEN));
        deleteResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        File deleteDir = new File("src/test/resources/packages/" + appInstanceId);
        String filePath = deleteDir.getCanonicalPath();
        File deletePath = new File(filePath);
        if (deletePath.isDirectory() == false) {
            return;
        }
        File[] listFiles = deletePath.listFiles();
        for (File fileSelected : listFiles) {
            fileSelected.delete();
        }
        deletePath.delete();
    }
}