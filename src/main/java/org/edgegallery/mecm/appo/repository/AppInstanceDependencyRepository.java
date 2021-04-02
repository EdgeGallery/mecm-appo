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


package org.edgegallery.mecm.appo.repository;

import java.util.List;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AppInstanceDependencyRepository extends CrudRepository<AppInstanceDependency, String> {
    @Query(value = "SELECT * FROM appinstancedependency m WHERE m.tenant =:tenant "
        + "AND m.dependency_app_instance_id =:dependency_app_instance_id",
            nativeQuery = true)
    List<AppInstanceDependency> getByDependencyAppInstanceId(@Param("tenant") String tenantId,
        @Param("dependency_app_instance_id") String dependencyAppInstanceId);

    @Query(value = "SELECT * FROM appinstancedependency m WHERE m.tenant =:tenant "
        + "AND m.app_instance_id =:app_instance_id",
            nativeQuery = true)
    List<AppInstanceDependency> getByAppInstanceId(@Param("tenant") String tenantId,
        @Param("app_instance_id") String appInstanceId);
}
