/*
 * Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.edgegallery.mecm.appo.service.impl;

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourcesNetworkReqParam;
import org.edgegallery.mecm.appo.service.ResourceMgrNetworkService;
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
public class ResourceMgrNetworkServiceImpl implements ResourceMgrNetworkService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrNetworkServiceImpl.class);

    @Autowired
    private RestServiceImpl restService;

    @Autowired
    ResourceMgrServiceHelper resourceMgrServiceHelper;

    @Autowired
    public ResourceMgrNetworkServiceImpl(RestServiceImpl restService,
                                         ResourceMgrServiceHelper resourceMgrServiceHelper) {
        this.restService = restService;
        this.resourceMgrServiceHelper = resourceMgrServiceHelper;
    }

    @Override
    public ResponseEntity<AppoV2Response> createNetworks(String accessToken, String tenantId, String hostId,
                                                ResourcesNetworkReqParam resourcesNetworkReqParam) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);
        LOGGER.info("request body: {}",resourceMgrServiceHelper.convertToJson(resourcesNetworkReqParam));

        LOGGER.debug("Create Flavor request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/networks").toString();
        return restService.sendRequestResourceManager(url, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(resourcesNetworkReqParam));
    }

    @Override
    public ResponseEntity<AppoV2Response> queryNetwork(String accessToken, String tenantId, String hostId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);

        LOGGER.debug("Query Networks request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/networks").toString();
        return restService.sendRequestResourceManager(url, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> queryNetworkById(String accessToken, String tenantId, String hostId,
                                                   String networkId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);

        LOGGER.debug("Query Networks by Id request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/networks/").append(networkId).toString();
        return restService.sendRequestResourceManager(url, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> deleteNetworkById(String accessToken, String tenantId, String hostId,
                                                    String networkId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info(Constants.URL, url);

        LOGGER.debug("Delete Networks by Id request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostId
                + "/networks/").append(networkId).toString();
        return restService.sendRequestResourceManager(url, HttpMethod.DELETE, accessToken, null);
    }
}
