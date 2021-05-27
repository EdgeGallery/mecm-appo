/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package org.edgegallery.mecm.appo.service;

import org.edgegallery.mecm.appo.apihandler.dto.AppInstantiateReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchCreateParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchInstancesReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.BatchTerminateReqParam;
import org.edgegallery.mecm.appo.apihandler.dto.CreateParam;
import org.edgegallery.mecm.appo.model.AppRule;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.springframework.http.ResponseEntity;

public interface AppoService {


    /**
     * Creates an application instance.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param createParam input parameters
     * @return application instance ID on success, error code on failure
     */

    ResponseEntity<AppoResponse> createAppInstance(String accessToken, String tenantId,
                                                   CreateParam createParam);


    /**
     * Batch creates an application instance.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param createParam input parameters
     * @return application instance IDs on success, error code on failure
     */

    ResponseEntity<AppoResponse> createAppInstance(String accessToken, String tenantId,
                                                   BatchCreateParam createParam);

    /**
     * Instantiates an application instance.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @param instantiationparams application instantiate parameters
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId, String appInstanceId,
                                                        AppInstantiateReqParam instantiationparams);


    /**
     * Batch instantiates an application instance.
     *
     * @param accessToken      access token
     * @param tenantId         tenant ID
     * @param appInstanceParam application instance IDs
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId,
                                                        BatchInstancesReqParam appInstanceParam);

    /**
     * Terminates an application instance.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<AppoResponse> terminateAppInstance(String accessToken, String tenantId, String appInstanceId);


    /**
     * Batch terminates application instances.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param batchTerminateParams application instance ID
     * @return status code 200 on success, error code on failure
     */
    ResponseEntity<AppoResponse> terminateAppInstance(String accessToken, String tenantId,
                                                             BatchTerminateReqParam batchTerminateParams);
    /**
     * Retrieves an application instance information.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return application instance info & status code 200 on success, error code on failure
     */

    ResponseEntity<AppoResponse> getAppInstance(String accessToken, String tenantId, String appInstanceId);

    /**
     * Retrieves edge host performance statistics.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      edge host IP
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<AppoResponse> queryKpi(String accessToken, String tenantId, String hostIp);

    /**
     * Retrieves edge host platform capabilities.
     *
     * @param accessToken  access token
     * @param tenantId     tenant ID
     * @param hostIp       edge host IP
     * @param capabilityId capability ID
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<AppoResponse> queryEdgehostCapabilities(String accessToken, String tenantId,
                                                           String hostIp, String capabilityId);

    /**
     * Configures application rules.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @param appRule       app rule input parameters
     * @param action        action
     * @return application instance ID on success, error code on failure
     */

    ResponseEntity<AppoResponse> configureAppRules(String accessToken, String tenantId, String appInstanceId,
                                                   AppRule appRule, String action);
}
