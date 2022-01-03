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

package org.edgegallery.mecm.appo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.edgegallery.mecm.appo.exception.ResourceMgrException;
import org.edgegallery.mecm.appo.service.impl.RestServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public final class ResourceMgrServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrServiceHelper.class);

    @Value("${appo.endpoints.inventory.end-point}")
    private String inventoryService;

    @Value("${appo.endpoints.inventory.port}")
    private String inventoryServicePort;

    @Autowired
    private RestServiceImpl restService;

    /**
     * Gets MEPM configurations from inventory.
     *
     * @param accessToken access token
     * @return returns mepm configurations
     * @throws ResourceMgrException exception if failed to get MEPm details
     */
    public String getInventoryMecHostsCfg(String accessToken, String tenantId, String hostIp) {

        LOGGER.info("teanant id is :" + tenantId);
        String url = new StringBuilder(inventoryService).append(":")
                .append(inventoryServicePort).append("/inventory/v1").append("/tenants/").append(tenantId)
            .append("/mechosts/").append(hostIp).toString();

        ResponseEntity<String> response = restService.sendRequest(url, HttpMethod.GET, accessToken, null);

        LOGGER.info("response: {}", response);
        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
        JsonElement mepmIp = jsonObject.get("mepmIp");
        if (mepmIp == null) {
            throw new ResourceMgrException("MEPM mepmIp is null for host " + hostIp);
        }

        return getInventoryMepmCfg(mepmIp.getAsString(), accessToken);
    }

    /**
     * Gets MEPM endpoint from inventory.
     *
     * @param hostIp      host ip
     * @param accessToken access token
     * @return returns MEPM config info
     * @throws ResourceMgrException exception if failed to get MEPM config details
     */
    private String getInventoryMepmCfg(String hostIp, String accessToken) {

        String url = new StringBuilder(inventoryService).append(":")
                .append(inventoryServicePort).append("/inventory/v1").append("/mepms/").append(hostIp).toString();

        ResponseEntity<String> response = restService.sendRequest(url, HttpMethod.GET, accessToken, null);

        LOGGER.info("response: {}", response);

        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
        JsonElement mepmPort = jsonObject.get("mepmPort");
        if (mepmPort == null) {
            throw new ResourceMgrException("MEPM port is null for host " + hostIp);
        }

        return hostIp + ":" + mepmPort.getAsString();
    }


    /**
     * Convert Object to JSON.
     * @throws ResourceMgrException exception if failed to get MEPM config details
     */
    public String convertToJson(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
