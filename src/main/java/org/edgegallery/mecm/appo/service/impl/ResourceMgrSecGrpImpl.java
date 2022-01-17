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

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrSecGrpParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrSecGrpRuleParam;
import org.edgegallery.mecm.appo.service.ResourceMgrSecGrpService;
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
public class ResourceMgrSecGrpImpl implements ResourceMgrSecGrpService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ResourceMgrSecGrpImpl.class);

    @Autowired
    private RestServiceImpl restService;

    @Autowired
    private ResourceMgrServiceHelper resourceMgrServiceHelper;

    @Autowired
    public ResourceMgrSecGrpImpl(RestServiceImpl restService, ResourceMgrServiceHelper resourceMgrServiceHelper) {
        this.restService = restService;
        this.resourceMgrServiceHelper = resourceMgrServiceHelper;
    }

    @Override
    public ResponseEntity<AppoV2Response> querySecurityGroups(String accessToken, String tenantId, String hostIp) {
        LOGGER.debug("Query security groups request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + "/securityGroups").toString();
        return restService.sendRequest_ResourceManager(apiUrl, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> querySecurityGroupRules(String accessToken, String tenantId, String hostIp,
                                                          String securityGroupId) {
        LOGGER.debug("Query security group rules request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.SECURITY_GROUPS).append(securityGroupId + "/securityGroupRules").toString();
        return restService.sendRequest_ResourceManager(apiUrl, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> querySecurityGroupById(String accessToken, String tenantId, String hostIp,
                                                         String securityGroupId) {
        LOGGER.debug("Query security group by ID request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.SECURITY_GROUPS + securityGroupId).toString();
        return restService.sendRequest_ResourceManager(apiUrl, HttpMethod.GET, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> deleteSecurityGroup(String accessToken, String tenantId, String hostIp,
                                                      String securityGroupId) {
        LOGGER.debug("Delete security groups request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.SECURITY_GROUPS + securityGroupId).toString();
        return restService.sendRequest_ResourceManager(apiUrl, HttpMethod.DELETE, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> deleteSecurityGroupRule(String accessToken, String tenantId, String hostIp,
                                                          String securityGroupId, String securityGroupRuleId) {
        LOGGER.debug("Delete security group rule request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.SECURITY_GROUPS
                + securityGroupId + "/securityGroupRules/" + securityGroupRuleId).toString();
        return restService.sendRequest_ResourceManager(apiUrl, HttpMethod.DELETE, accessToken, null);
    }

    @Override
    public ResponseEntity<AppoV2Response> createSecurityGroup(String accessToken, String tenantId, String hostIp,
                                                      ResourceMgrSecGrpParam securityGroupParam) {
        LOGGER.debug("Create security group request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + "/securityGroups").toString();
        return restService.sendRequest_ResourceManager(apiUrl, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(securityGroupParam));
    }

    @Override
    public ResponseEntity<AppoV2Response> createSecurityGroupRule(String accessToken, String tenantId, String hostIp,
                                                                  String securityGroupId,
                                                                  ResourceMgrSecGrpRuleParam securityGroupRuleParam) {
        LOGGER.debug("Create security group rule request received...");
        String apiUrl = resourceMgrServiceHelper.getInventoryMecHostsCfg(accessToken, tenantId, hostIp);
        StringBuilder sb = new StringBuilder(apiUrl);
        apiUrl = sb.append(Constants.RESOURCE_CONTROLLER_URI).append(tenantId).append(Constants.HOSTS).append(hostIp
                + Constants.SECURITY_GROUPS + securityGroupId + "/securityGroupRules").toString();
        return restService.sendRequest_ResourceManager(apiUrl, HttpMethod.POST, accessToken,
                resourceMgrServiceHelper.convertToJson(securityGroupRuleParam));
    }
}
