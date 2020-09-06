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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppoApplicationTest.class)
@AutoConfigureMockMvc
public class AppOrchestratorHandlerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser(roles = "MECM_TENANT")
    public void validateAppLcmInventory() throws Exception {
        String tenantId = "12db0288-3c67-4042-a708-a8e4a10c6b31";

        ResultActions postResult =
                mvc.perform(MockMvcRequestBuilders.post("/appo/v1/tenants/" + tenantId + "/app_instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(
                                "{ \"appPackageId\": \"f20358433cf8eb4719a62a49ed118c9b\", \"appName\": \"face_recognition\", "
                                        + "\"appdId\": \"f50358433cf8eb4719a62a49ed118c9b\", "
                                        + "\"appInstanceDescription\": \"face_recognition\", "
                                        + "\"mecHost\": \"172.17.0.10\" }")
                        .header("access_token", "SampleToken"));

        MvcResult postMvcResult = postResult.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        String postResponse = postMvcResult.getResponse().getContentAsString();
        assertThat(postResponse, containsString("app_instance_id"));
    }
}
