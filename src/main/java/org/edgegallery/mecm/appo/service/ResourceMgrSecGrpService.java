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

import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrSecGrpParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrSecGrpRuleParam;
import org.springframework.http.ResponseEntity;

public interface ResourceMgrSecGrpService {

    /**
     * Retrieves security groups.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      host IP
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> querySecurityGroups(String accessToken, String tenantId, String hostIp);

    /**
     * Retrieves security groups based on ID.
     *
     * @param accessToken       access token
     * @param tenantId          tenant ID
     * @param hostIp            edge host IP
     * @param securityGroupId   security group ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> querySecurityGroupById(String accessToken, String tenantId, String hostIp,
                                                  String securityGroupId);

    /**
     * Retrieves security group rules.
     *
     * @param accessToken       access token
     * @param tenantId          tenant ID
     * @param hostIp            edge host IP
     * @param securityGroupId   security group ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> querySecurityGroupRules(String accessToken, String tenantId, String hostIp,
                                                   String securityGroupId);

    /**
     * Delete security groups.
     *
     * @param accessToken       access token
     * @param tenantId          tenant ID
     * @param hostIp            edge host IP
     * @param securityGroupId   security group ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> deleteSecurityGroup(String accessToken, String tenantId, String hostIp,
                                               String securityGroupId);

    /**
     * Delete security group rules.
     *
     * @param accessToken       access token
     * @param tenantId          tenant ID
     * @param hostIp            edge host IP
     * @param securityGroupId   security group ID
     * @param securityGroupRuleId   security group rule ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<String> deleteSecurityGroupRule(String accessToken, String tenantId, String hostIp,
                                                   String securityGroupId, String securityGroupRuleId);

    /**
     * Create security groups.
     *
     * @param accessToken           access token
     * @param tenantId              tenant ID
     * @param hostIp                edge host IP
     * @param securityGroupParams   security group parameters
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<String> createSecurityGroup(String accessToken, String tenantId, String hostIp,
                                               ResourceMgrSecGrpParam securityGroupParams);

    /**
     * Create security group rules.
     *
     * @param accessToken           access token
     * @param tenantId              tenant ID
     * @param hostIp                edge host IP
     * @param securityGroupId       security group ID
     * @param securityGroupRuleParams   security group rule parameters
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<String> createSecurityGroupRule(String accessToken, String tenantId,
                                                   String hostIp, String securityGroupId,
                                                   ResourceMgrSecGrpRuleParam securityGroupRuleParams);
}
