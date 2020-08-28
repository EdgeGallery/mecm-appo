package org.edgegallery.mecm.appo.service;

import java.util.List;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;

public interface AppInstanceInfoService {

    /**
     * Retrieves application instance information.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     * @return application instance information
     */
    AppInstanceInfo getAppInstanceInfo(String tenantId, String appInstanceId);

    /**
     * Retrieves all application instance information.
     *
     * @param tenantId tenant ID
     * @return application instance information
     */
    List<AppInstanceInfo> getAllAppInstanceInfo(String tenantId);

    /**
     * Creates application instance info.
     *
     * @param tenantId        tenant ID
     * @param appInstanceInfo application information
     * @return application information
     */
    AppInstanceInfo createAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo);

    /**
     * Deletes application instance information.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId application instance ID
     */
    void deleteAppInstanceInfo(String tenantId, String appInstanceId);

    /**
     * Updates application instance info.
     *
     * @param tenantId        tenant ID
     * @param appInstanceInfo application information
     * @return application information
     */
    AppInstanceInfo updateAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo);
}
