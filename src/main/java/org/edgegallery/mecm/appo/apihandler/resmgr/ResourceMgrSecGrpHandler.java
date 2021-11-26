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

package org.edgegallery.mecm.appo.apihandler.resmgr;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrSecGrpParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrSecGrpRuleParam;
import org.edgegallery.mecm.appo.service.ResourceMgrSecGrpService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Resource mananger api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class ResourceMgrSecGrpHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceMgrSecGrpHandler.class);

    private final ResourceMgrSecGrpService resourceMgrService;

    public ResourceMgrSecGrpHandler(ResourceMgrSecGrpService resourceMgrService) {
        this.resourceMgrService = resourceMgrService;
    }

    /**
     * Retrieves security groups.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves security groups", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/securityGroups")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "query security group success.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> querySecurityGroup(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp) {
        logger.debug("Query security groups request received...");
        return resourceMgrService.querySecurityGroups(accessToken, tenantId, hostIp);
    }

    /**
     * Retrieves security groups.
     *
     * @param tenantId          tenant ID
     * @param hostIp            edge host IP
     * @param securityGroupId   security group ID
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves security group by id", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/securityGroups/{security_group_id}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "query security group success.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> querySecurityGroupById(@ApiParam(value = "access token")
                                                 @RequestHeader("access_token") String accessToken,
                                                 @PathVariable("tenant_id")
                                                 @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                 @Size(max = 64) String tenantId,
                                                 @ApiParam(value = "edge host ip")
                                                 @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                 @Size(max = 15) String hostIp,
                                                 @PathVariable("security_group_id") String securityGroupId) {
        logger.debug("Query security group by ID request received...");
        return resourceMgrService.querySecurityGroupById(accessToken, tenantId, hostIp, securityGroupId);
    }

    /**
     * Retrieves security group rules.
     *
     * @param tenantId          tenant ID
     * @param hostIp            edge host IP
     * @param securityGroupId   security group ID
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves security group rules", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/securityGroups/{security_group_id}/securityGroupRules")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "query security group rules.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> querySecurityGroupRules(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp,
                                              @PathVariable("security_group_id") String securityGroupId) {
        logger.debug("Query Images request received...");
        return resourceMgrService.querySecurityGroupRules(accessToken, tenantId, hostIp, securityGroupId);
    }

    /**
     * Delete seuciryt groups.
     *
     * @param tenantId         tenant ID
     * @param hostIp           edge host IP
     * @param securityGroupId  security group ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Delete security groups", response = String.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/securityGroups/{security_group_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "groups deleted success.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> deleteSecurityGroup(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp,
                                              @ApiParam(value = "security group id")
                                              @PathVariable("security_group_id") String securityGroupId) {
        logger.debug("Delete security group request received...");
        return resourceMgrService.deleteSecurityGroup(accessToken, tenantId, hostIp, securityGroupId);
    }

    /**
     * Delete security group rules.
     *
     * @param tenantId         tenant ID
     * @param hostIp           edge host IP
     * @param securityGroupId  security group ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Delete security group rules", response = String.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/securityGroups/{security_group_id}/securityGroupRules"
            + "/{security_group_rule_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Rule deleted successfully. ", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> deleteSecurityGroupRule(@ApiParam(value = "access token")
                                                      @RequestHeader("access_token") String accessToken,
                                                      @PathVariable("tenant_id")
                                                      @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                      @Size(max = 64) String tenantId,
                                                      @ApiParam(value = "edge host ip")
                                                      @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                      @Size(max = 15) String hostIp,
                                                      @ApiParam(value = "security group id")
                                                      @PathVariable("security_group_id") String securityGroupId,
                                                      @ApiParam(value = "security group rule id")
                                                      @PathVariable("security_group_rule_id")
                                                                      String securityGroupRuleId) {
        logger.debug("Delete security group rules request received...");
        return resourceMgrService.deleteSecurityGroupRule(accessToken, tenantId, hostIp, securityGroupId,
                securityGroupRuleId);
    }

    /**
     * Create security groups.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 201 on success, error code on failure
     */
    @ApiOperation(value = "create image", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/securityGroups",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "group created successfully.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> createSecurityGroup(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp,
                                              @ApiParam(value = "security group")
                                              @Valid @RequestBody ResourceMgrSecGrpParam securityGroupBody) {
        logger.debug("Create security groups request received...");
        return resourceMgrService.createSecurityGroup(accessToken, tenantId, hostIp, securityGroupBody);
    }

    /**
     * Create security group rules.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 201 on success, error code on failure
     */
    @ApiOperation(value = "create security group rule", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/securityGroups/{security_group_id}/securityGroupRules",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "rules created successfully.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> createSecurityGroupRule(@ApiParam(value = "access token")
                                                      @RequestHeader("access_token") String accessToken,
                                                      @PathVariable("tenant_id")
                                                      @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                      @Size(max = 64) String tenantId,
                                                      @ApiParam(value = "edge host ip")
                                                      @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                      @Size(max = 15) String hostIp,
                                                      @PathVariable("security_group_id") String securityGroupId,
                                                      @ApiParam(value = "security group rule")
                                                      @Valid @RequestBody ResourceMgrSecGrpRuleParam
                                                                      securityGroupRuleBody) {
        logger.debug("Create security group rules request received...");
        return resourceMgrService.createSecurityGroupRule(accessToken, tenantId, hostIp, securityGroupId,
                securityGroupRuleBody);
    }
}
