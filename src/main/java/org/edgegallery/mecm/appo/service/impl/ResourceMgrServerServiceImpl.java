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

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.OperateVmParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourcesServerReqParam;
import org.edgegallery.mecm.appo.service.ResourceMgrServerService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.ResourceMgrServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResourceMgrServerServiceImpl implements ResourceMgrServerService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrServerServiceImpl.class);

    @Autowired
    private RestServiceImpl restService;

    @Autowired
    private ResourceMgrServiceHelper resourceMgrServiceHelper;

    @Autowired
    public ResourceMgrServerServiceImpl(RestServiceImpl restService,
                                        ResourceMgrServiceHelper resourceMgrServiceHelper) {
        this.restService = restService;
        this.resourceMgrServiceHelper = resourceMgrServiceHelper;
    }

    @Override
    public ResponseEntity<String> createServers(String accessToken, String tenantId, String hostId,
                                                ResourcesServerReqParam resourcesServerReqParam) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info("url: {}", url);
        LOGGER.info("request body: {}", resourceMgrServiceHelper.convertToJson(resourcesServerReqParam));

        LOGGER.debug("Create Servers request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append("/hosts/").append(hostId
                + "/servers").toString();
        return restService.sendRequest(url, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(resourcesServerReqParam));
    }

    @Override
    public ResponseEntity<String> operateVM(String accessToken, String tenantId, String hostId,
                                            String serverId, OperateVmParam operateVmParam) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info("url: {}", url);
        LOGGER.info("request body: {}",resourceMgrServiceHelper.convertToJson(operateVmParam));

        LOGGER.debug("Operate VM request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append("/hosts/").append(hostId
                + "/servers/").append(serverId).toString();
        return restService.sendRequest(url, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(operateVmParam));
    }

    @Override
    public ResponseEntity<String> queryServers(String accessToken, String tenantId, String hostId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info("url: {}", url);

        LOGGER.debug("Query Servers request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append("/hosts/").append(hostId
                + "/servers").toString();
        return restService.sendRequest(url, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<String> queryServerById(String accessToken, String tenantId, String hostId,
                                                  String serverId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info("url: {}", url);

        LOGGER.debug("Query Servers by Id request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append("/hosts/").append(hostId
                + "/servers/").append(serverId).toString();
        return restService.sendRequest(url, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<String> deleteServerById(String accessToken, String tenantId, String hostId,
                                                   String serverId) {
        String url = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostId);
        LOGGER.info("url: {}", url);

        LOGGER.debug("Delete Servers by Id request received...");
        StringBuilder sb = new StringBuilder(url);
        url = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append("/hosts/").append(hostId
                + "/servers/").append(serverId).toString();
        return restService.sendRequest(url, HttpMethod.DELETE, accessToken, null);
    }
}
