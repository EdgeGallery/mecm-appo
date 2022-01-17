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

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceFlavorReqParam;
import org.edgegallery.mecm.appo.service.ResourceMgrFlavorService;
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
public class ResourceMgrFlavorServiceImpl implements ResourceMgrFlavorService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrFlavorServiceImpl.class);

    @Autowired
    private RestServiceImpl restService;

    @Autowired
    ResourceMgrServiceHelper resourceMgrServiceHelper;

    @Autowired
    public ResourceMgrFlavorServiceImpl(RestServiceImpl restService,
                                        ResourceMgrServiceHelper resourceMgrServiceHelper) {
        this.restService = restService;
        this.resourceMgrServiceHelper = resourceMgrServiceHelper;
    }

    @Override
    public ResponseEntity<AppoV2Response> createFlavor(String accessToken, String tenantId,
                                               String hostId, ResourceFlavorReqParam resourceFlavorReqParam) {

        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);
        LOGGER.debug("Create Flavor request received...");
        LOGGER.info("request body: {}", resourceMgrServiceHelper.convertToJson(resourceFlavorReqParam));
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/flavors").toString();
        return restService.sendRequestResourceManager(url, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(resourceFlavorReqParam));
    }

    @Override
    public ResponseEntity<AppoV2Response> queryFlavors(String accessToken, String tenantId, String hostId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);

        LOGGER.debug("Query Flavors request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/flavors").toString();
        return restService.sendRequestResourceManager(url, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> queryFlavorsById(String accessToken, String tenantId,
                                                   String hostId, String flavorId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);

        LOGGER.debug("Query Flavors by Id request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/flavors/").append(flavorId).toString();
        return restService.sendRequestResourceManager(url, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> deleteFlavorsById(String accessToken, String tenantId, String hostId,
                                                            String flavorId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);

        LOGGER.debug("Delete Flavors by Id request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/flavors/").append(flavorId).toString();
        return restService.sendRequestResourceManager(url, HttpMethod.DELETE, accessToken, null);
    }

}
