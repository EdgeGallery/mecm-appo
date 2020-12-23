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

package org.edgegallery.mecm.appo.repository;

import java.util.List;
import org.edgegallery.mecm.appo.model.AppRuleTask;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AppRuleTaskRepository extends CrudRepository<AppRuleTask, String> {

    @Query(value = "SELECT * FROM appruletask m WHERE m.tenant=:tenant", nativeQuery = true)
    List<AppRuleTask> findByTenantId(@Param("tenant") String tenant);

    @Query(value = "SELECT * FROM appruletask m WHERE m.tenant=:tenant and m.app_rule_task_id=:app_rule_task_id",
            nativeQuery = true)
    AppRuleTask findByTenantIdAndAppRuleTaskId(@Param("tenant") String tenant,
                                               @Param("app_rule_task_id") String appRuleTaskId);
}
