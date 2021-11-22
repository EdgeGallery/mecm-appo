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

import static org.edgegallery.mecm.appo.utils.Constants.APPD_ID_REGEX;
import static org.edgegallery.mecm.appo.utils.Constants.APP_INST_ID_REGX;
import static org.edgegallery.mecm.appo.utils.Constants.APP_PKG_ID_REGX;
import static org.edgegallery.mecm.appo.utils.Constants.TENENT_ID_REGEX;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstanceInfoDto;
import org.edgegallery.mecm.appo.apihandler.dto.AppPkgInstanceInfoDto;
import org.edgegallery.mecm.appo.apihandler.dto.AppRuleTaskDto;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppRuleTask;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.edgegallery.mecm.appo.utils.AppoV2Response;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.ErrorMessage;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Appo database API handler.
 */
@RestSchema(schemaId = "appo-db")
@Api(value = "Application instance info api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class AppoDbHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppoDbHandler.class);

    private final AppInstanceInfoService appInstanceInfoService;

    @Autowired
    public AppoDbHandler(AppInstanceInfoService appInstanceInfoService) {
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
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<AppoResponse> getAppInstanceInfo(
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "application instance id") @PathVariable("appInstance_id")
            @Pattern(regexp = APP_INST_ID_REGX) @Size(max = 64) String appInstanceId) {

        LOGGER.info("Retrieve application instance info: {}", appInstanceId);

        AppInstanceInfo appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);
        ModelMapper mapper = new ModelMapper();
        AppInstanceInfoDto dto = mapper.map(appInstanceInfo, AppInstanceInfoDto.class);

        return new ResponseEntity<>(new AppoResponse(dto), HttpStatus.OK);
    }

    /**
     * Retrieves application instance information.
     *
     * @param tenantId       tenant ID
     * @param appinstanceids applicationm instance IDs
     * @return application instance information
     */
    @ApiOperation(value = "Retrieves application instance info", response = AppoResponse.class)
    @GetMapping(value = "/tenants/{tenant_id}/app_instance_infos", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<AppoResponse> getAllAppInstanceInfo(
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @RequestParam(required = false) @Size(max = 50)
                    List<@Pattern(regexp = APP_INST_ID_REGX) String> appinstanceids) {

        LOGGER.info("Retrieve application instance infos");

        List<AppInstanceInfoDto> appInstanceInfosDto = new LinkedList<>();
        List<AppInstanceInfo> appInstanceInfos = appInstanceInfoService.getAllAppInstanceInfo(tenantId);
        if (appInstanceInfos == null || appInstanceInfos.isEmpty()) {
            LOGGER.error("app instance info does not exist");
            throw new NoSuchElementException(Constants.RECORD_NOT_FOUND);
        }

        if (appinstanceids == null) {
            for (AppInstanceInfo tenantAppInstanceInfo : appInstanceInfos) {
                ModelMapper mapper = new ModelMapper();
                appInstanceInfosDto.add(mapper.map(tenantAppInstanceInfo, AppInstanceInfoDto.class));
            }
            return new ResponseEntity<>(new AppoResponse(appInstanceInfosDto), HttpStatus.OK);
        }

        for (AppInstanceInfo tenantAppInstanceInfo : appInstanceInfos) {
            ModelMapper mapper = new ModelMapper();
            if (appinstanceids.contains(tenantAppInstanceInfo.getAppInstanceId())) {
                appInstanceInfosDto.add(mapper.map(tenantAppInstanceInfo, AppInstanceInfoDto.class));
            }
        }
        if (appInstanceInfosDto.isEmpty()) {
            LOGGER.error("app instance info does not exist");
            throw new NoSuchElementException(Constants.RECORD_NOT_FOUND);
        }
        return new ResponseEntity<>(new AppoResponse(appInstanceInfosDto), HttpStatus.OK);
    }

    /**
     * Retrieves application instance information.
     *
     * @param tenantId      tenant ID
     * @param appRuleTaskId application rule task ID
     * @return application instance information
     */
    @ApiOperation(value = "Retrieves application rule task info", response = AppoResponse.class)
    @GetMapping(path = "/tenants/{tenant_id}/apprule_task_infos/{apprule_task_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<AppoResponse> getAppRuletaskInfo(
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "application instance id") @PathVariable("apprule_task_id")
            @Pattern(regexp = APP_INST_ID_REGX) @Size(max = 64) String appRuleTaskId) {

        LOGGER.info("Retrieve application rule task info: {}", appRuleTaskId);

        AppRuleTask appRuletaskInfo = appInstanceInfoService.getAppRuleTaskInfo(tenantId, appRuleTaskId);
        ModelMapper mapper = new ModelMapper();
        AppRuleTaskDto dto = mapper.map(appRuletaskInfo, AppRuleTaskDto.class);

        return new ResponseEntity<>(new AppoResponse(dto), HttpStatus.OK);
    }

    /**
     * Retrieves application instance info based on package ID.
     *
     * @param tenantId      tenant ID
     * @param appId application ID
     * @param appPkgId application package ID
     * @return application instantiation information
     */
    @ApiOperation(value = "Retrieves application instance info based on package ID", response = AppoV2Response.class)
    @GetMapping(path = "/tenants/{tenant_id}/apps/{app_id}/packages/{app_package_id}/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN') || hasRole('MECM_GUEST')")
    public ResponseEntity<AppoV2Response> getAppPkgInstantiationStatus(
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "app id") @PathVariable("app_id")
            @Pattern(regexp = APPD_ID_REGEX) @Size(max = 64) String appId,
            @ApiParam(value = "app package id") @PathVariable("app_package_id")
            @Pattern(regexp = APP_PKG_ID_REGX) @Size(max = 64) String appPkgId) {

        LOGGER.info("Retrieve application instance info based on package id: {}", appPkgId);

        List<AppInstanceInfo> appInstanceInfo = appInstanceInfoService.getAppInstanceInfoByPkgId(tenantId,
                appId, appPkgId);
        List<AppPkgInstanceInfoDto> data = new LinkedList<>();
        AppPkgInstanceInfoDto instanceInfo;
        for (AppInstanceInfo info: appInstanceInfo) {
            ModelMapper mapper = new ModelMapper();
            instanceInfo = mapper.map(info, AppPkgInstanceInfoDto.class);
            data.add(instanceInfo);
        }

        return new ResponseEntity<>(new AppoV2Response(data, new ErrorMessage(HttpStatus.OK.value(),
                Collections.<String>emptyList()), ""), HttpStatus.OK);
    }
}