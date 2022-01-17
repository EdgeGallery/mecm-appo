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

package org.edgegallery.mecm.appo.service.impl;

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrImageParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrImportParam;
import org.edgegallery.mecm.appo.service.ResourceMgrImageService;
import org.edgegallery.mecm.appo.utils.AppoV2Response;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.ResourceMgrServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResourceMgrImageImpl implements ResourceMgrImageService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrImageImpl.class);

    @Autowired
    private RestServiceImpl restService;

    @Autowired
    ResourceMgrServiceHelper resourceMgrServiceHelper;

    @Autowired
    public ResourceMgrImageImpl(RestServiceImpl restService, ResourceMgrServiceHelper resourceMgrServiceHelper) {
        this.restService = restService;
        this.resourceMgrServiceHelper = resourceMgrServiceHelper;
    }

    @Override
    public ResponseEntity<AppoV2Response> queryImages(String accessToken, String tenantId, String hostIp) {
        LOGGER.debug("Query images request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + "/images").toString();
        return restService.sendRequestResourceManager(apiUrl, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> queryImagesById(String accessToken, String tenantId, String hostIp,
                                                          String imageId) {
        LOGGER.debug("Query image by ID request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.IMAGES + imageId).toString();
        return restService.sendRequestResourceManager(apiUrl, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> deleteImage(String accessToken, String tenantId, String hostIp,
                                                      String imageId) {
        LOGGER.debug("Delete image request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.IMAGES + imageId).toString();
        return restService.sendRequestResourceManager(apiUrl, HttpMethod.DELETE, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> createImage(String accessToken, String tenantId, String hostIp,
                                              ResourceMgrImageParam resourceMgrImageParam) {
        LOGGER.debug("Create image request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + "/images").toString();
        LOGGER.info("request body: {}", resourceMgrServiceHelper.convertToJson(resourceMgrImageParam));
        return restService.sendRequestResourceManager(apiUrl, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(resourceMgrImageParam));
    }

    @Override
    public ResponseEntity<AppoV2Response> importImage(String accessToken, String tenantId, String hostIp,
                                                      String imageId, ResourceMgrImportParam resourceMgrImportParam) {
        LOGGER.debug("Import image request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.IMAGES + imageId).toString();
        LOGGER.info("request body: {}", resourceMgrServiceHelper.convertToJson(resourceMgrImportParam));
        return restService.sendRequestResourceManager(apiUrl, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(resourceMgrImportParam));
    }

}
