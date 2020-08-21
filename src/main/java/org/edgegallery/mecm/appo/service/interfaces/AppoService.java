package org.edgegallery.mecm.appo.service.interfaces;

import java.util.List;
import java.util.Map;
import org.edgegallery.mecm.appo.apihandler.CreateParam;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
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

    public ResponseEntity<Map<String, String>> createAppInstance(String accessToken,
                                                                 String tenantId,
                                                                 CreateParam createParam);

    /**
     * Instantiates an application instance.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return status code 200 on success, error code on failure
     */

    public ResponseEntity<String> instantiateAppInstance(String accessToken,
                                                         String tenantId,
                                                         String appInstanceId);

    /**
     * Retrieves an application instance information.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return application instance info & status code 200 on success, error code on failure
     */

    public ResponseEntity<AppInstanceInfo> getAppInstance(String accessToken,
                                                          String tenantId,
                                                          String appInstanceId);

    /**
     * Retrieves all application instance information.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @return all application instances & status code 200 on success, error code on failure
     */

    public ResponseEntity<List<AppInstanceInfo>> getAllAppInstance(String accessToken,
                                                                   String tenantId);

    /**
     * Terminates an application instance.
     *
     * @param accessToken   access token
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return status code 200 on success, error code on failure
     */

    public ResponseEntity<String> terminateAppInstance(String accessToken,
                                                       String tenantId,
                                                       String appInstanceId);

    /**
     * Retrieves edge host performance statistics.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      edge host IP
     * @return status code 200 on success, error code on failure
     */

    public ResponseEntity<String> queryKpi(String accessToken,
                                           String tenantId,
                                           String hostIp);

    /**
     * Retrieves edge host platform capabilities.
     *
     * @param accessToken access token
     * @param tenantId    tenant ID
     * @param hostIp      edge host IP
     * @return status code 200 on success, error code on failure
     */

    public ResponseEntity<String> queryEdgehostCapabilities(String accessToken,
                                                            String tenantId,
                                                            String hostIp);
}
