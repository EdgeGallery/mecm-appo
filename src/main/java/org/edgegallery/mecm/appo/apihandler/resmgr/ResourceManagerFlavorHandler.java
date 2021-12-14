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
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceFlavorReqParam;
import org.edgegallery.mecm.appo.service.ResourceMgrFlavorService;
import org.edgegallery.mecm.appo.utils.AppoV2Response;
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
 * Flavor API handler.
 */
@Api(value = "Flavor api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class ResourceManagerFlavorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagerFlavorHandler.class);

    private final ResourceMgrFlavorService resourceMgrFlavorService;

    public ResourceManagerFlavorHandler(ResourceMgrFlavorService resourceMgrFlavorService) {
        this.resourceMgrFlavorService = resourceMgrFlavorService;
    }

    /**
     * Create Flavor.
     *
     * @param tenantId      tenant ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Create Flavor", response = AppoV2Response.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/flavors",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoV2Response.class),
            @ApiResponse(code = 500, message = "internal server error", response = AppoV2Response.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoV2Response> createFlavor(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "edge host ip")
            @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
            @Size(max = 15) String hostIp,
            @ApiParam(value = "Instantiate application instances")
            @Valid @RequestBody(required = false) ResourceFlavorReqParam resourceFlavorReqParam) {

        logger.debug("Create Flavor request received...");

        return resourceMgrFlavorService.createFlavor(accessToken, tenantId, hostIp, resourceFlavorReqParam);
    }

    /**
     * Retrieves Flavors.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves Flavors", response = AppoV2Response.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/flavors")
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<AppoV2Response> queryFlavors(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp) {
        logger.debug("Query Flavors request received...");

        return resourceMgrFlavorService.queryFlavors(accessToken, tenantId, hostIp);
    }

    /**
     * Retrieves Flavors by ID.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves flavor by Id", response = AppoV2Response.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/flavors/{flavor_id}")
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<AppoV2Response> queryFlavorsById(@ApiParam(value = "access token")
                                               @RequestHeader("access_token") String accessToken,
                                               @PathVariable("tenant_id")
                                               @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                               @Size(max = 64) String tenantId,
                                               @ApiParam(value = "edge host ip")
                                               @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                               @Size(max = 15) String hostIp,
                                               @ApiParam(value = "flavor id")
                                               @PathVariable("flavor_id")
                                               @Size(max = 64) String flavorId) {
        logger.debug("Query flavor by Id received...");

        return resourceMgrFlavorService.queryFlavorsById(accessToken, tenantId, hostIp, flavorId);
    }

    /**
     * Delete Flavors.
     *
     * @param flavorId      flavor ID
     * @param tenantId      tenant ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Terminates Flavors By Id", response = AppoV2Response.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/flavors/{flavor_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoV2Response.class),
            @ApiResponse(code = 500, message = "internal server error", response = AppoV2Response.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoV2Response> deleteFlavorsById(
                                    @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
                                    @ApiParam(value = "tenant id") @PathVariable("tenant_id")
                                    @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
                                    @ApiParam(value = "edge host ip")
                                    @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                    @Size(max = 15) String hostIp,
                                    @ApiParam(value = "flavor id")
                                    @PathVariable("flavor_id")
                                    @Size(max = 64) String flavorId) {
        logger.debug("Terminate Flavors by Id request received...");

        return resourceMgrFlavorService.deleteFlavorsById(accessToken, tenantId, hostIp, flavorId);
    }

}
