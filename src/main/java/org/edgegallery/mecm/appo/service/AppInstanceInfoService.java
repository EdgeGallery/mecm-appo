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
     */
    void createAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo);

    /**
     * Create the dependency info of instance.
     *
     * @param tenantId           tenant ID
     * @param dependencies  app instance dependencies
     */
    void createAppInstanceDependencyInfo(String tenantId, List<AppInstanceDependency> dependencies);

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
     */
    void updateAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo);

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
     */
    void createAppRuleTaskInfo(String tenantId, AppRuleTask appRuleTaskInfo);

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
     */
    void updateAppRuleTaskInfo(String tenantId, AppRuleTask appRuleTaskInfo);
}
