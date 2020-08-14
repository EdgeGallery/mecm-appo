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
import java.util.List;
import java.util.Map;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application orchestrator API handler.
 */
@Api(value = "Application orchestrator api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class AppOrchestratorHandler {

    // TODO pre authorization & parameter validations

    /**
     * Creates an application instance.
     *
     * @param tenantId    tenant ID
     * @param createParam input parameters for creation request
     * @return application instance ID on success, error code on failure
     */
    @ApiOperation(value = "Creates application instance", response = Map.class)
    @RequestMapping(path = "/tenants/{tenant_id}/app_instances",
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createAppInstance(@PathVariable("tenant_id") String tenantId,
                                                                 @RequestBody CreateParam createParam) {
        // TODO: implementation
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Instantiates an application instance.
     *
     * @param tenantId         tenant ID
     * @param appInstanceId    application instance ID
     * @param instantiateParam input parameters for instantiate request
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Instantiate application instance", response = String.class)
    @RequestMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}",
            method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> instantiateAppInstance(@PathVariable("tenant_id") String tenantId,
                                                         @PathVariable("app_instance_id") String appInstanceId,
                                                         @RequestBody InstantiateParam instantiateParam) {
        // TODO: implementation
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieves an application instance information.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return application instance info & status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves application instance information", response = AppInstanceInfo.class)
    @RequestMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppInstanceInfo> getAppInstance(@PathVariable("tenant_id") String tenantId,
                                                          @PathVariable("app_instance_id") String appInstanceId) {
        // TODO: implementation
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieves all application instance information.
     *
     * @param tenantId tenant ID
     * @return all application instances & status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves application instance information", response = List.class)
    @RequestMapping(path = "/tenants/{tenant_id}/app_instances",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AppInstanceInfo>> getAllAppInstance(@PathVariable("tenant_id") String tenantId) {
        // TODO: implementation
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Terminates an application instance.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Terminates application instance", response = String.class)
    @RequestMapping(path = "/tenant/{tenant_id}/app_instances/{app_instance_id}",
            method = RequestMethod.DELETE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> terminateAppInstance(@PathVariable("tenant_id") String tenantId,
                                                       @PathVariable("app_instance_id") String appInstanceId) {
        // TODO: implementation
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieves edge host performance statistics.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves edge host performance statistics", response = String.class)
    @RequestMapping(path = "/tenant/{tenant_id}/hosts/{host_ip}/kpi",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> queryKpi(@PathVariable("tenant_id") String tenantId,
                                           @PathVariable("host_ip") String hostIp) {
        // TODO: implementation
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieves edge host platform capabilities.
     *
     * @param tenantId tenant ID
     * @param hostIp   edge host IP
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Retrieves edge host platform capabilities", response = String.class)
    @RequestMapping(path = "/tenant/{tenant_id}/hosts/{host_ip}/mep_capabilities",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> queryEdgehostCapabilities(@PathVariable("tenant_id") String tenantId,
                                                            @PathVariable("host_ip") String hostIp) {
        // TODO: implementation
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*
    TODO: TBD
    1. Application container usage info from host
     */
}
