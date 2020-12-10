package org.edgegallery.mecm.appo.service;

import java.util.List;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppRuleTask;

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
     * Retrieves application instance information by mec host.
     *
     * @param tenantId tenant ID
     * @param mecHost  mec host
     * @return application instance information
     */
    List<AppInstanceInfo> getAppInstanceInfoByMecHost(String tenantId, String mecHost);

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

    /**
     * Creates application instance dependencies information.
     *
     * @param tenantId                tenant ID
     * @param appInstanceDependencies application dependencies information
     */
    void createAppInstanceDependencies(String tenantId, List<AppInstanceDependency> appInstanceDependencies);

    /**
     * Retrieves application instance dependencies information.
     *
     * @param tenantId                tenant ID
     * @param dependencyAppInstanceId dependency application instance ID
     * @return dependencies information
     */
    List<AppInstanceDependency> getDependenciesByDependencyAppInstanceId(String tenantId,
                                                                         String dependencyAppInstanceId);

    /**
     * Retrieves application rule task information.
     *
     * @param tenantId      tenant ID
     * @param appRuleTaskId application rule task ID
     * @return application instance information
     */
    AppRuleTask getAppRuleTaskInfo(String tenantId, String appRuleTaskId);

    /**
     * Creates application rule task info.
     *
     * @param tenantId        tenant ID
     * @param appRuleTaskInfo application rule task
     * @return application rule task information
     */
    AppRuleTask createAppRuleTaskInfo(String tenantId, AppRuleTask appRuleTaskInfo);

    /**
     * Deletes application rule task information.
     *
     * @param tenantId      tenant ID
     * @param appRuleTaskId application rule task ID
     */
    void deleteAppRuleTaskInfo(String tenantId, String appRuleTaskId);

    /**
     * Updates application rule task info.
     *
     * @param tenantId        tenant ID
     * @param appRuleTaskInfo application rule task information
     * @return application rule task information
     */
    AppRuleTask updateAppRuleTaskInfo(String tenantId, AppRuleTask appRuleTaskInfo);
}
