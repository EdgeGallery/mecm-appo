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

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrImageParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrImportParam;
import org.springframework.http.ResponseEntity;

public interface ResourceMgrImageService {

    /**
     * Retrieves images.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      edge host IP
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> queryImages(String accessToken, String tenantId, String hostIp);

    /**
     * Retrieves images.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      edge host IP
     * @param imageId     imageId
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> queryImagesById(String accessToken, String tenantId, String hostIp, String imageId);

    /**
     * Delete images.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      edge host IP
     * @param imageId     image ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> deleteImage(String accessToken, String tenantId, String hostIp, String imageId);

    /**
     * Create images.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      edge host IP
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<String> createImage(String accessToken, String tenantId, String hostIp,
                                       ResourceMgrImageParam resourceMgrImageParam);

    /**
     * Import images.
     *
     * @param accessToken               access token
     * @param tenantId                  tenant ID
     * @param hostIp                    edge host IP
     * @param imageId                   image ID
     * @param resourceMgrImageParam     import parameters
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<String> importImage(String accessToken, String tenantId, String hostIp, String imageId,
                                       ResourceMgrImportParam resourceMgrImageParam);
}
