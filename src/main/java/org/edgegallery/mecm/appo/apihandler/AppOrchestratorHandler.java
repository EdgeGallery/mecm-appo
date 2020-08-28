/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.edgegallery.mecm.appo.apihandler;

import static org.edgegallery.mecm.appo.common.Constants.TENENT_ID_REGEX;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.AppoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * Application orchestrator API handler.
 */
@Api(value = "Application orchestrator api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class AppOrchestratorHandler {

    private static final Logger logger = LoggerFactory.getLogger(AppOrchestratorHandler.class);

    private final AppoService appoService;

    @Autowired
    public AppOrchestratorHandler(AppoService appoService) {
        this.appoService = appoService;
    }

    /**
     * Creates an application instance.
     *
     * @param tenantId    tenant ID
     * @param createParam input parameters for creation request
     * @return application instance ID on success, error code on failure
     */
    @ApiOperation(value = "Creates application instance", response = Map.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = String.class),
                           @ApiResponse(code = 500, message = "internal server error", response = String.class)
                })
    public ResponseEntity<Map<String, String>> createAppInstance(
                                    @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
                                    @ApiParam(value = "tenant id") @PathVariable("tenant_id")
                                    @Pattern(regexp = TENENT_ID_REGEX) String tenantId,
                                    @ApiParam(value = "create application instance")
                                    @Valid @RequestBody CreateParam createParam) {
        logger.debug("Application create request received...");

        return appoService.createAppInstance(accessToken, tenantId, createParam);
    }

    /**
     * Instantiates an application instance.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Instantiate application instance", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = String.class),
                           @ApiResponse(code = 500, message = "internal server error", response = String.class)
                })
    public ResponseEntity<String> instantiateAppInstance(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) String tenantId,
            @ApiParam(value = "application instance id") @PathVariable("app_instance_id") String appInstanceId) {
        logger.debug("Application instantiation request received...");

        return appoService.instantiateAppInstance(accessToken, tenantId, appInstanceId);
    }

    /**
     * Retrieves an application instance information.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return application instance info & status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves application instance information", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAppInstance(@ApiParam(value = "access token")
                                                 @RequestHeader("access_token") String accessToken,
                                                 @ApiParam(value = "tenant id") @PathVariable("tenant_id")
                                                 @Pattern(regexp = TENENT_ID_REGEX) String tenantId,
                                                 @ApiParam(value = "application instance id")
                                                 @PathVariable("app_instance_id") String appInstanceId) {
        logger.debug("Query application info request received...");

        return appoService.getAppInstance(accessToken, tenantId, appInstanceId);
    }

    /**
     * Retrieves all application instance information.
     *
     * @param tenantId tenant ID
     * @return all application instances & status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves application instance information", response = List.class)
    @GetMapping(path = "/tenants/{tenant_id}/app_instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AppInstanceInfo>> getAllAppInstance(@ApiParam(value = "access token")
                                                                   @RequestHeader("access_token") String accessToken,
                                                                   @ApiParam(value = "tenant id")
                                                                   @PathVariable("tenant_id")
                                                                   @Pattern(regexp = TENENT_ID_REGEX) String tenantId) {
        logger.debug("Query all application info request received...");

        return appoService.getAllAppInstance(accessToken, tenantId);
    }

    /**
     * Terminates an application instance.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Terminates application instance", response = String.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = String.class),
                           @ApiResponse(code = 500, message = "internal server error", response = String.class)
                })
    public ResponseEntity<String> terminateAppInstance(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) String tenantId,
            @ApiParam(value = "application instance id") @PathVariable("app_instance_id") String appInstanceId) {
        logger.debug("Terminate application info request received...");

        return appoService.terminateAppInstance(accessToken, tenantId, appInstanceId);
    }

    /**
     * Retrieves edge host performance statistics.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves edge host performance statistics", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/kpi", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> queryKpi(@ApiParam(value = "access token")
                                           @RequestHeader("access_token") String accessToken,
                                           @PathVariable("tenant_id")
                                           @Pattern(regexp = TENENT_ID_REGEX) String tenantId,
                                           @ApiParam(value = "edge host ip") @PathVariable("host_ip") String hostIp) {
        logger.debug("Query KPI request received...");

        return appoService.queryKpi(accessToken, tenantId, hostIp);
    }

    /**
     * Retrieves edge host platform capabilities.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves edge host platform capabilities", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/mep_capabilities",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> queryEdgehostCapabilities(@ApiParam(value = "access token")
                                                            @RequestHeader("access_token") String accessToken,
                                                            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
                                                            @Pattern(regexp = TENENT_ID_REGEX) String tenantId,
                                                            @ApiParam(value = "edge host ip")
                                                            @PathVariable("host_ip") String hostIp) {
        logger.debug("Query MEP capabilities request received...");

        return appoService.queryEdgehostCapabilities(accessToken, tenantId, hostIp);
    }
}
