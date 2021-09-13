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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstantiateReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchCreateParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchInstancesReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchTerminateReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.CreateParam;
import org.edgegallery.mecm.appo.service.AppoService;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
 * Application orchestrator API handler.
 */
@RestSchema(schemaId = "appo-appInstance")
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
    @ApiOperation(value = "Creates application instance", response = AppoResponse.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoResponse.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoResponse> createAppInstance(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
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
    @ApiOperation(value = "Instantiate application instance", response = AppoResponse.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoResponse.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoResponse> instantiateAppInstance(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "application instance id")
            @PathVariable("app_instance_id") @Pattern(regexp = Constants.APP_INST_ID_REGX)
            @Size(max = 64) String appInstanceId,
            @ApiParam(value = "Instantiate application instances")
            @Valid @RequestBody(required = false) AppInstantiateReqParam instantiateParam) {

        logger.debug("Application instantiation request received...");

        return appoService.instantiateAppInstance(accessToken, tenantId, appInstanceId, instantiateParam);
    }

    /**
     * Retrieves an application instance information.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return application instance info & status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves application instance information", response = AppoResponse.class)
    @GetMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<AppoResponse> getAppInstance(@ApiParam(value = "access token")
                                                       @RequestHeader("access_token") String accessToken,
                                                       @ApiParam(value = "tenant id") @PathVariable("tenant_id")
                                                       @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                       @Size(max = 64) String tenantId,
                                                       @ApiParam(value = "application instance id")
                                                       @PathVariable("app_instance_id")
                                                       @Pattern(regexp = Constants.APP_INST_ID_REGX)
                                                       @Size(max = 64) String appInstanceId) {
        logger.debug("Query application info request received...");

        return appoService.getAppInstance(accessToken, tenantId, appInstanceId);
    }

    /**
     * Terminates an application instance.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Terminates application instance", response = AppoResponse.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoResponse.class),
            @ApiResponse(code = 500, message = "internal server error", response = AppoResponse.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoResponse> terminateAppInstance(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "application instance id")
            @PathVariable("app_instance_id") @Pattern(regexp = Constants.APP_INST_ID_REGX)
            @Size(max = 64) String appInstanceId) {
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
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryKpi(@ApiParam(value = "access token")
                                                 @RequestHeader("access_token") String accessToken,
                                                 @PathVariable("tenant_id")
                                                 @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                 @Size(max = 64) String tenantId,
                                                 @ApiParam(value = "edge host ip")
                                                 @PathVariable("host_ip") @Pattern(regexp = Constants.HOST_IP_REGX)
                                                 @Size(max = 15) String hostIp) {
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
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryEdgehostCapabilities(@ApiParam(value = "access token")
                                                                  @RequestHeader("access_token") String accessToken,
                                                                  @ApiParam(value = "tenant id")
                                                                  @PathVariable("tenant_id")
                                                                  @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                                  @Size(max = 64) String tenantId,
                                                                  @ApiParam(value = "edge host ip")
                                                                  @PathVariable("host_ip")
                                                                  @Pattern(regexp = Constants.HOST_IP_REGX)
                                                                  @Size(max = 15) String hostIp) {
        logger.debug("Query MEP capabilities request received...");

        return appoService.queryEdgehostCapabilities(accessToken, tenantId, hostIp, null);
    }

    /**
     * Retrieves edge host platform capability.
     *
     * @param tenantId     tenant ID
     * @param hostIp       edge host IP
     * @param capabilityId capability ID
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves edge host platform capabilities", response = String.class)
    @GetMapping(path = "/tenants/{tenant_id}/hosts/{host_ip}/mep_capabilities/{capability_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<String> queryEdgehostCapability(@ApiParam(value = "access token")
                                                                @RequestHeader("access_token") String accessToken,
                                                                @ApiParam(value = "tenant id")
                                                                @PathVariable("tenant_id")
                                                                @Pattern(regexp = Constants.TENENT_ID_REGEX)
                                                                @Size(max = 64) String tenantId,
                                                                @ApiParam(value = "edge host ip")
                                                                @PathVariable("host_ip")
                                                                @Pattern(regexp = Constants.HOST_IP_REGX)
                                                                @Size(max = 15) String hostIp,
                                                                @PathVariable("capability_id")
                                                                @Pattern(regexp = Constants.APP_NAME_REGEX)
                                                                @Size(max = 64) String capabilityId) {
        logger.debug("Query MEP capabilities request received...");

        return appoService.queryEdgehostCapabilities(accessToken, tenantId, hostIp, capabilityId);
    }

    /**
     * Queries liveness & readiness.
     *
     * @return status code 200 when ready
     */
    @ApiOperation(value = "Queries liveness and readiness", response = String.class)
    @GetMapping(path = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    /**
     * Batch create application instances.
     *
     * @param tenantId    tenant ID
     * @param createParam input parameters for creation request
     * @return application instance IDs on success, error code on failure
     */
    @ApiOperation(value = "Batch creates application instances", response = AppoResponse.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances/batch_create", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoResponse.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoResponse> batchCreateAppInstance(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "create application instance")
            @Valid @RequestBody BatchCreateParam createParam) {
        logger.debug("Application create request received...");

        return appoService.createAppInstance(accessToken, tenantId, createParam);
    }

    /**
     * Batch instantiate application instances.
     *
     * @param tenantId         tenant ID
     * @param appInstanceParam application instance parameters
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Batch instantiate application instances", response = AppoResponse.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances/batch_instantiate",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoResponse.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoResponse> batchInstantiateAppInstance(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "Instantiate application instances")
            @Valid @RequestBody BatchInstancesReqParam appInstanceParam) {
        logger.debug("Application instantiation request received...");

        return appoService.instantiateAppInstance(accessToken, tenantId, appInstanceParam);
    }

    /**
     * Batch terminates application instances.
     *
     * @param tenantId         tenant ID
     * @param appInstanceParam application instance parameters
     * @return status code 201, error code on failure
     */
    @ApiOperation(value = "Batch terminate application instances", response = AppoResponse.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances/batch_terminate",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "request accepted ", response = AppoResponse.class),
            @ApiResponse(code = 500, message = "internal server error", response = String.class)
    })
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoResponse> batchTerminateAppInstances(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "Batch terminate application instances")
            @Valid @RequestBody BatchTerminateReqParam appInstanceParam) {
        logger.debug("Batch terminate application instance request received...");

        return appoService.terminateAppInstance(accessToken, tenantId, appInstanceParam);
    }
}
