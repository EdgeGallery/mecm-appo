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
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrImageParam;
import org.edgegallery.mecm.appo.apihandler.resmgr.dto.ResourceMgrImportParam;
import org.edgegallery.mecm.appo.service.ResourceMgrImageService;
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
public class ResourceMgrImageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceMgrImageHandler.class);

    private final ResourceMgrImageService resourceMgrService;

    public ResourceMgrImageHandler(ResourceMgrImageService resourceMgrService) {
        this.resourceMgrService =  resourceMgrService;
    }

    /**
     * Retrieves images.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves images", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/images")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "query images successfully.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryImages(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp) {
        logger.debug("Query image request received...");
        return resourceMgrService.queryImages(accessToken, tenantId, hostIp);
    }

    /**
     * Retrieves images.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @param imageId  image ID
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves images", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/images/{image_id}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "query images successfully.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryImageById(@ApiParam(value = "access token")
                                                 @RequestHeader("access_token") String accessToken,
                                                 @PathVariable("tenant_id")
                                                 @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                 @Size(max = 64) String tenantId,
                                                 @ApiParam(value = "edge host ip")
                                                 @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                 @Size(max = 15) String hostIp,
                                                 @PathVariable("image_id") String imageId) {
        logger.debug("Query image request received...");
        return resourceMgrService.queryImagesById(accessToken, tenantId, hostIp, imageId);
    }

    /**
     * Delete images.
     *
     * @param tenantId      tenant ID
     * @param hostIp        edge host IP
     * @param imageId       image ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Delete Images", response = String.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/images/{image_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "image deleted successfully. ", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> deleteImage(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp,
                                              @ApiParam(value = "image id")
                                              @PathVariable("image_id") String imageId) {
        logger.debug("Delete image request received...");
        return resourceMgrService.deleteImage(accessToken, tenantId, hostIp, imageId);
    }

    /**
     * Create images.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 201 on success, error code on failure
     */
    @ApiOperation(value = "create image", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/images",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Image created successfully.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> createImage(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp,
                                              @ApiParam(value = "create image")
                                              @Valid @RequestBody ResourceMgrImageParam resourceMgrImageParam) {
        logger.debug("Create image request received...");
        return resourceMgrService.createImage(accessToken, tenantId, hostIp, resourceMgrImageParam);
    }

    /**
     * Import images.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @param imageId image ID
     * @return status code 201 on success, error code on failure
     */
    @ApiOperation(value = "import images", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/images/{image_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Image imported successfully.", response = String.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    public ResponseEntity<String> importImage(@ApiParam(value = "access token")
                                              @RequestHeader("access_token") String accessToken,
                                              @PathVariable("tenant_id")
                                              @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                              @Size(max = 64) String tenantId,
                                              @ApiParam(value = "edge host ip")
                                              @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                              @Size(max = 15) String hostIp,
                                              @ApiParam(value = "image id")
                                              @PathVariable("image_id") String imageId,
                                              @ApiParam(value = "import image")
                                              @Valid @RequestBody ResourceMgrImportParam resourceMgrImageParam) {
        logger.debug("Import image request received...");
        return resourceMgrService.importImage(accessToken, tenantId, hostIp, imageId, resourceMgrImageParam);
    }
}
