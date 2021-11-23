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
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourcesNetworkReqParam;
import org.edgegallery.mecm.appo.service.ResourceMgrNetworkService;
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

/**
 * ResourceManager Networks API handler.
 */
@Api(value = "ResourceManager Networks api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class ResourceManagerNetworkHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagerNetworkHandler.class);

    private final ResourceMgrNetworkService resourceMgrNetworkService;

    public ResourceManagerNetworkHandler(ResourceMgrNetworkService resourceMgrNetworkService) {
        this.resourceMgrNetworkService =  resourceMgrNetworkService;
    }

    /**
     * Create Networks.
     *
     * @param tenantId      tenant ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Create Networks", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/networks",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<String> createNetworks(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "edge host ip")
            @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
            @Size(max = 15) String hostIp,
            @ApiParam(value = "Create Networks")
            @Valid @RequestBody(required = false) ResourcesNetworkReqParam resourcesNetworkReqParam) {

        logger.debug("Network Creation request received...");

        return resourceMgrNetworkService.createNetworks(accessToken, tenantId, hostIp, resourcesNetworkReqParam);
    }

    /**
     * Retrieves Networks.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves Networks", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/networks")
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryNetworks(@ApiParam(value = "access token")
                                               @RequestHeader("access_token") String accessToken,
                                               @PathVariable("tenant_id")
                                               @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                               @Size(max = 64) String tenantId,
                                               @ApiParam(value = "edge host ip")
                                               @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                               @Size(max = 15) String hostIp) {
        logger.debug("Query Networks request received...");

        return resourceMgrNetworkService.queryNetwork(accessToken, tenantId, hostIp);
    }

    /**
     * Retrieves Networks by ID.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves Networks by Id", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/networks/{network_id}")
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryNetworkById(@ApiParam(value = "access token")
                                                   @RequestHeader("access_token") String accessToken,
                                                   @PathVariable("tenant_id")
                                                   @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                   @Size(max = 64) String tenantId,
                                                   @ApiParam(value = "edge host ip")
                                                   @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                   @Size(max = 15) String hostIp,
                                                   @ApiParam(value = "network id")
                                                   @PathVariable("network_id")
                                                   @Size(max = 64) String networkId) {
        logger.debug("Query Netwoks by Id received...");

        return resourceMgrNetworkService.queryNetworkById(accessToken, tenantId, hostIp, networkId);
    }

    /**
     * Delete Networks.
     *
     * @param networkId      network ID
     * @param tenantId      tenant ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Terminates Networks By Id", response = String.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/networks/{network_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<String> deleteFlavorsById(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "edge host ip")
            @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
            @Size(max = 15) String hostIp,
            @ApiParam(value = "network id")
            @PathVariable("network_id")
            @Size(max = 64) String networkId) {
        logger.debug("Terminate Networks by Id request received...");

        return resourceMgrNetworkService.deleteNetworkById(accessToken, tenantId, hostIp, networkId);
    }
}
