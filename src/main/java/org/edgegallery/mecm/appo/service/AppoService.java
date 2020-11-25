package org.edgegallery.mecm.appo.service;

import org.edgegallery.mecm.appo.apihandler.BatchCreateParam;
import org.edgegallery.mecm.appo.apihandler.BatchInstancesParam;
import org.edgegallery.mecm.appo.apihandler.CreateParam;
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
     * @return status code 200 on success, error code on failure
     */

    ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId, String appInstanceId);


    /**
     * Batch instantiates an application instance.
     *
     * @param accessToken      access token
     * @param tenantId         tenant ID
     * @param appInstanceParam application instance IDs
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<AppoResponse> instantiateAppInstance(String accessToken, String tenantId,
                                                        BatchInstancesParam appInstanceParam);


    /**
     * Batch terminates application instances.
     *
     * @param accessToken       access token
     * @param tenantId          tenant ID
     * @param appInstanceparams application instance parameters
     * @return status code 201 on success, error code on failure
     */

    ResponseEntity<AppoResponse> terminateAppInstance(String accessToken, String tenantId,
                                                      BatchInstancesParam appInstanceparams);

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
}
