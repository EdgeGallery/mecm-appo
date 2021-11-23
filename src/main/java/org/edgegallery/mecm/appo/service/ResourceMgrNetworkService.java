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


package org.edgegallery.mecm.appo.service;

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourcesNetworkReqParam;
import org.springframework.http.ResponseEntity;

public interface ResourceMgrNetworkService {


    /**
     * Create Networks.
     *
     * @param accessToken              access token
     * @param hostId                   host ID
     * @param tenantId                 tenant ID
     * @param resourcesNetworkReqParam networks parameters
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<String> createNetworks(String accessToken, String tenantId, String hostId,
                                         ResourcesNetworkReqParam resourcesNetworkReqParam);

    /**
     * Query Network.
     *
     * @param accessToken access token
     * @param hostId      host ID
     * @param tenantId    tenant ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> queryNetwork(String accessToken, String tenantId, String hostId);

    /**
     * Query Network By ID.
     *
     * @param accessToken   access token
     * @param networkId     network ID
     * @param hostId        host ID
     * @param tenantId      tenant ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> queryNetworkById(String accessToken, String tenantId, String hostId,
                                            String networkId);

    /**
     * Delete Network By ID.
     *
     * @param accessToken   access token
     * @param networkId     network ID
     * @param hostId        host ID
     * @param tenantId      tenant ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> deleteNetworkById(String accessToken, String tenantId, String hostId,
                                             String networkId);

}