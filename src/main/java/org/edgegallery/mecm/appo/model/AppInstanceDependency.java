package org.edgegallery.mecm.appo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appinstancedependency")
public final class AppInstanceDependency {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "app_instance_id")
    private String appInstanceId;
    @Column(name = "dependency_app_instance_id")
    private String dependencyAppInstanceId;
    @Column(name = "tenant")
    private String tenant;
}
