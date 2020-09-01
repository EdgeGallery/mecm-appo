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

import static org.edgegallery.mecm.appo.utils.Constants.APP_INST_ID_REGX;
import static org.edgegallery.mecm.appo.utils.Constants.TENENT_ID_REGEX;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstanceInfoDto;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application instance info API handler.
 */
@Api(value = "Application instance info api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class AppInstanceInfoHandler {

    private static final Logger logger = LoggerFactory.getLogger(AppInstanceInfoHandler.class);

    private final AppInstanceInfoService appInstanceInfoService;

    @Autowired
    public AppInstanceInfoHandler(AppInstanceInfoService appInstanceInfoService) {
        this.appInstanceInfoService = appInstanceInfoService;
    }

    /**
     * Retrieves application instance information.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return application instance information
     */
    @ApiOperation(value = "Retrieves application instance info", response = AppoResponse.class)
    @GetMapping(path = "/tenants/{tenant_id}/app_instance_infos/{appInstance_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT')")
    public ResponseEntity<AppoResponse> getAppInstanceInfo(
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "application instance id") @PathVariable("appInstance_id")
            @Pattern(regexp = APP_INST_ID_REGX) @Size(max = 64) String appInstanceId) {

        logger.info("Retrieve application instance info: {}", appInstanceId);

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        ModelMapper mapper = new ModelMapper();
        AppInstanceInfoDto dto = mapper.map(appInstanceInfo, AppInstanceInfoDto.class);

        return new ResponseEntity<>(new AppoResponse(dto), HttpStatus.OK);
    }

    /**
     * Retrieves application instance information.
     *
     * @param tenantId tenant ID
     * @return application instance information
     */
    @ApiOperation(value = "Retrieves application instance info", response = AppoResponse.class)
    @GetMapping(value = "/tenants/{tenant_id}/app_instance_infos", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT')")
    public ResponseEntity<AppoResponse> getAllAppInstanceInfo(
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) @Size(max = 64) String tenantId) {

        logger.info("Retrieve application instance infos");

        List<AppInstanceInfoDto> appInstanceInfosDto = new LinkedList<>();
        List<AppInstanceInfo> appInstanceInfos = appInstanceInfoService.getAllAppInstanceInfo(tenantId);
        for (AppInstanceInfo tenantAppInstanceInfo : appInstanceInfos) {
            ModelMapper mapper = new ModelMapper();
            appInstanceInfosDto.add(mapper.map(tenantAppInstanceInfo, AppInstanceInfoDto.class));
        }

        return new ResponseEntity<>(new AppoResponse(appInstanceInfosDto), HttpStatus.OK);
    }
}