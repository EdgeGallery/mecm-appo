package org.edgegallery.mecm.appo.repository;

import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppInstanceDependencyRepository extends CrudRepository<AppInstanceDependency, String> {
    @Query(value = "SELECT * FROM appinstancedependency m WHERE m.tenant =:tenant AND m.dependency_app_instance_id =:dependency_app_instance_id",
            nativeQuery = true)
    List<AppInstanceDependency> getByDependencyAppInstanceId(@Param("tenant") String tenantId,
                                                             @Param("dependency_app_instance_id") String dependencyAppInstanceId);

    @Query(value = "SELECT * FROM appinstancedependency m WHERE m.tenant =:tenant AND m.app_instance_id =:app_instance_id",
            nativeQuery = true)
    List<AppInstanceDependency> getByAppInstanceId(@Param("tenant") String tenantId, @Param("app_instance_id") String appInstanceId);
}
