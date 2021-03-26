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

package org.edgegallery.mecm.appo.apihandler;

import static org.edgegallery.mecm.appo.utils.Constants.TENENT_ID_REGEX;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstanceDeletedDto;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstanceInfoDto;
import org.edgegallery.mecm.appo.apihandler.dto.SyncDeletedAppInstanceDto;
import org.edgegallery.mecm.appo.apihandler.dto.SyncUpdatedAppInstanceDto;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.service.RestServiceImpl;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Appo database API handler.
 */
@Api(value = "Application instance info api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class AppoSyncHandler {

    private static final Logger logger = LoggerFactory.getLogger(AppoSyncHandler.class);

    @Value("${appo.endpoints.inventory.end-point}")
    private String inventoryService;

    @Value("${appo.endpoints.inventory.port}")
    private String inventoryServicePort;

    @Autowired
    private RestServiceImpl syncService;

    @Autowired
    private AppInstanceInfoService appInstanceInfoService;

    /**
     * Synchronizes application instance info form all edges.
     *
     * @param tenantId       tenant ID
     * @return application instance information
     */
    @ApiOperation(value = "Synchronizes application instance info form all edge", response = AppoResponse.class)
    @PostMapping(value = "/tenants/{tenant_id}/app_instance_infos/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT') || hasRole('MECM_ADMIN')")
    public ResponseEntity<AppoResponse> syncAppInstanceInfos(@ApiParam(value = "access token")
            @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant id") @PathVariable("tenant_id")
            @Pattern(regexp = TENENT_ID_REGEX) @Size(max = 64) String tenantId) {
        synchronizeAppInstancesInfoFromEdges(tenantId, accessToken);
        return new ResponseEntity<>(new AppoResponse("accepted"), HttpStatus.ACCEPTED);
    }

    @Async
    private void synchronizeAppInstancesInfoFromEdges(String tenantId,  String accessToken) {
        logger.info("Sync application instance infos from edge");
        try {
            Set<String> applcms = getInventoryMecHostsCfg(accessToken);
            for (String applcm: applcms) {
                logger.info("Sync application instance infos from edge {}", applcm);
                String appLcmEndPoint = getInventoryApplcmCfg(applcm, accessToken);

                getSyncAppInstanceStaleRecords(appLcmEndPoint, tenantId, accessToken);
                getSyncAppInstanceUpdateRecords(appLcmEndPoint, tenantId, accessToken);
            }
        } catch (AppoException ex) {
            throw new AppoException("failed to synchronize app instance info from edge:" + ex.getMessage());
        }
    }

    private void getSyncAppInstanceUpdateRecords(String appLcmEndPoint, String tenantId, String accessToken) {
        try {
            String uri = appLcmEndPoint + "/lcmcontroller/v1" + "/tenants/" + tenantId + "/app_instances/sync_updated";

            ResponseEntity<SyncUpdatedAppInstanceDto> updateResponse = syncService.syncRecords(uri,
                    SyncUpdatedAppInstanceDto.class, accessToken);
            SyncUpdatedAppInstanceDto syncUpdatedAppInstDto = updateResponse.getBody();
            // Update table
            String[] applcmIp = appLcmEndPoint.split(":");
            if (syncUpdatedAppInstDto != null && syncUpdatedAppInstDto.getAppInstanceUpdatedRecs() != null) {
                for (AppInstanceInfoDto updatedRecord : syncUpdatedAppInstDto.getAppInstanceUpdatedRecs()) {
                    updateSyncAppInstanceRecords(tenantId, applcmIp[0], updatedRecord);
                }
            }
        } catch (NoSuchElementException ex) {
            logger.error("failed to sync records {}", ex.getMessage());
        }
    }

    private void getSyncAppInstanceStaleRecords(String appLcmEndPoint, String tenantId, String accessToken) {
        try {
            String uri = appLcmEndPoint + "/lcmcontroller/v1" + "/tenants/" + tenantId + "/app_instances/sync_deleted";

            ResponseEntity<SyncDeletedAppInstanceDto> updateResponse = syncService.syncRecords(uri,
                    SyncDeletedAppInstanceDto.class, accessToken);
            SyncDeletedAppInstanceDto syncDeletedAppInstDto = updateResponse.getBody();
            // Update table
            if (syncDeletedAppInstDto != null && syncDeletedAppInstDto.getAppInstanceDeletedRecs() != null) {
                for (AppInstanceDeletedDto deletedRecord : syncDeletedAppInstDto.getAppInstanceDeletedRecs()) {
                    deleteSyncAppInstanceRecords(tenantId, deletedRecord);
                }
            }
        } catch (AppoException ex) {
            logger.error("failed to sync records {}", ex.getMessage());
        }
    }

    private void updateSyncAppInstanceRecords(String tenantId, String applcmIp, AppInstanceInfoDto updatedRecord) {
        ModelMapper mapper = new ModelMapper();
        updatedRecord.setApplcmHost(applcmIp);
        if (updatedRecord.getOperationalStatus() == null) {
            updatedRecord.setOperationalStatus("Instantiated");
        }

        try {
            appInstanceInfoService.getAppInstanceInfo(tenantId, updatedRecord.getAppInstanceId());

            // record already exist update it
            appInstanceInfoService.updateAppInstanceInfo(tenantId, mapper.map(updatedRecord, AppInstanceInfo.class));

        } catch (NoSuchElementException e) {
            //record does not exist add new
            AppInstanceInfo appInstanceInfo = mapper.map(updatedRecord, AppInstanceInfo.class);
            if (appInstanceInfo.getAppPackageId() != null) {
                appInstanceInfo.setAppId(appInstanceInfo.getAppPackageId().substring(0, 32));
            }
            appInstanceInfoService.createAppInstanceInfo(tenantId, appInstanceInfo);
        }
    }

    private void deleteSyncAppInstanceRecords(String tenantId, AppInstanceDeletedDto deletedRecord) {
        try {
            appInstanceInfoService.deleteAppInstanceInfo(tenantId, deletedRecord.getAppInstanceId());
        } catch (NoSuchElementException e) {
            logger.error("app instance does not exist to delete");
        }
    }

    /**
     * Gets applcm endpoint from inventory.
     *
     * @param hostIp      host ip
     * @param accessToken access token
     * @return returns edge repository info
     * @throws AppoException exception if failed to get edge repository details
     */
    private String getInventoryApplcmCfg(String hostIp, String accessToken) {

        String url = new StringBuilder(inventoryService).append(":")
                .append(inventoryServicePort).append("/inventory/v1").append("/applcms/").append(hostIp).toString();

        ResponseEntity<String> response = syncService.sendRequest(url, HttpMethod.GET, accessToken, null);

        logger.info("response: {}", response);

        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
        JsonElement applcmPort = jsonObject.get("applcmPort");
        if (applcmPort == null) {
            throw new AppoException("applcm port is null for host " + hostIp);
        }

        return hostIp + ":" + applcmPort.getAsString();
    }

    /**
     * Gets applcm configurations from inventory.
     *
     * @param accessToken access token
     * @return returns applcms
     * @throws AppoException exception if failed to get edge repository details
     */
    private Set<String> getInventoryMecHostsCfg(String accessToken) {

        String url = new StringBuilder(inventoryService).append(":")
                .append(inventoryServicePort).append("/inventory/v1").append("/mechosts/").toString();

        ResponseEntity<String> response = syncService.sendRequest(url, HttpMethod.GET, accessToken, null);

        logger.info("response: {}", response);
        JsonArray jsonArray = new JsonParser().parse(response.getBody()).getAsJsonArray();

        Set<String> applcms = new HashSet<>();
        String applcm;
        for (JsonElement host: jsonArray) {
            applcm = host.getAsJsonObject().get("applcmIp").getAsString();
            applcms.add(applcm);
        }

        return applcms;
    }
}