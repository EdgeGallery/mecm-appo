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

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.OperateVmParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourcesServerReqParam;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.springframework.http.ResponseEntity;

public interface ResourceMgrServerService {


    /**
     * Create Servers.
     *
     * @param accessToken              access token
     * @param hostId                   host ID
     * @param tenantId                 tenant ID
     * @param resourcesServerReqParam  servers parameters
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<String> createServers(String accessToken, String tenantId, String hostId,
                                          ResourcesServerReqParam resourcesServerReqParam);

    /**
     * Query Servers.
     *
     * @param accessToken access token
     * @param hostId      host ID
     * @param tenantId    tenant ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> queryServers(String accessToken, String tenantId, String hostId);

    /**
     * Query Server By ID.
     *
     * @param accessToken   access token
     * @param serverId      server ID
     * @param hostId        host ID
     * @param tenantId      tenant ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> queryServerById(String accessToken, String tenantId, String hostId,
                                            String serverId);

    /**
     * Operate VM.
     *
     * @param accessToken    access token
     * @param serverId       server ID
     * @param hostIp         host IP
     * @param tenantId       tenant ID
     * @param operateVmParam operate vm
     * @return status code 200 on success, error code on failure
     */
    ResponseEntity<String> operateVM(String accessToken, String tenantId, String hostIp, String serverId,
                                     OperateVmParam operateVmParam);

    /**
     * Delete Server By ID.
     *
     * @param accessToken   access token
     * @param serverId      server ID
     * @param hostId        host ID
     * @param tenantId      tenant ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> deleteServerById(String accessToken, String tenantId, String hostId,
                                             String serverId);

}