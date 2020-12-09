package org.edgegallery.mecm.appo.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appruletask")
public final class AppRuleTask {

    @Id
    @Column(name = "app_rule_task_id")
    private String appRuleTaskId;

    @Column(name = "tenant")
    private String tenant;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "app_instance_id")
    private String appInstanceId;

    @Column(name = "app_rules")
    private String appRules;

    @Column(name = "config_result")
    private String configResult;

    @Column(name = "detail")
    private String detailed;

    @Column(name = "create_time")
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
