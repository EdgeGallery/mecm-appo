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
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.OperateVmParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourcesServerReqParam;
import org.edgegallery.mecm.appo.service.ResourceMgrServerService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * ResourceManager Server API handler.
 */
@Api(value = "ResourceManager Server api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class ResourceManagerServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagerServerHandler.class);

    private final ResourceMgrServerService resourceMgrServerService;

    public ResourceManagerServerHandler(ResourceMgrServerService resourceMgrServerService) {
        this.resourceMgrServerService = resourceMgrServerService;
    }

    /**
     * Create Servers.
     *
     * @param tenantId      tenant ID
     * @param hostIp      host IP
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Create Servers", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/servers",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<String> createServers(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "edge host ip")
            @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
            @Size(max = 15) String hostIp,
            @ApiParam(value = "Create Servers")
            @Valid @RequestBody(required = false) ResourcesServerReqParam resourcesServerReqParam) {

        logger.debug("Server Creation request received...");

        return resourceMgrServerService.createServers(accessToken, tenantId, hostIp, resourcesServerReqParam);
    }

    /**
     * Retrieves Servers.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves Servers", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/servers", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryServers(@ApiParam(value = "access token")
                                               @RequestHeader("access_token") String accessToken,
                                               @PathVariable("tenant_id")
                                               @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                               @Size(max = 64) String tenantId,
                                               @ApiParam(value = "edge host ip")
                                               @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                               @Size(max = 15) String hostIp) {
        logger.debug("Query Servers request received...");

        return resourceMgrServerService.queryServers(accessToken, tenantId, hostIp);
    }

    /**
     * Retrieves Servers by ID.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves Servers by Id", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/servers/{server_id}")
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryServerById(@ApiParam(value = "access token")
                                                   @RequestHeader("access_token") String accessToken,
                                                   @PathVariable("tenant_id")
                                                   @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                   @Size(max = 64) String tenantId,
                                                   @ApiParam(value = "edge host ip")
                                                   @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                   @Size(max = 15) String hostIp,
                                                   @ApiParam(value = "server id")
                                                   @PathVariable("server_id")
                                                   @Size(max = 64) String serverId) {
        logger.debug("Query Servers by Id received...");

        return resourceMgrServerService.queryServerById(accessToken, tenantId, hostIp, serverId);
    }

    /**
     * Retrieves Servers by ID.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves Servers by Id", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/servers/{server_id}")
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> operateVM(@ApiParam(value = "access token")
                                                  @RequestHeader("access_token") String accessToken,
                                                  @PathVariable("tenant_id")
                                                  @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                  @Size(max = 64) String tenantId,
                                                  @ApiParam(value = "edge host ip")
                                                  @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                  @Size(max = 15) String hostIp,
                                                  @ApiParam(value = "server id")
                                                  @PathVariable("server_id")
                                                  @Size(max = 64) String serverId,
                                                  @ApiParam(value = "Create Servers")
                                                  @Valid @RequestBody(required = false) OperateVmParam oprateVmParam) {
        logger.debug("Query Servers by Id received...");

        return resourceMgrServerService.operateVM(accessToken, tenantId, hostIp, serverId, oprateVmParam);
    }

    /**
     * Delete Servers.
     *
     * @param serverId      server ID
     * @param tenantId      tenant ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Terminates Servers By Id", response = String.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/servers/{server_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<String> deleteServersById(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "edge host ip")
            @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
            @Size(max = 15) String hostIp,
            @ApiParam(value = "server id")
            @PathVariable("server_id")
            @Size(max = 64) String serverId) {
        logger.debug("Terminate Servers by Id request received...");

        return resourceMgrServerService.deleteServerById(accessToken, tenantId, hostIp, serverId);
    }
}