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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appinstanceinfo")
public final class AppInstanceInfo {

    @Id
    @Column(name = "app_instance_id")
    private String appInstanceId;

    @Column(name = "app_package_id")
    private String appPackageId;

    @Column(name = "tenant")
    private String tenant;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "appd_id")
    private String appdId;

    @Column(name = "app_descriptor")
    private String appDescriptor;

    @Column(name = "mec_host")
    private String mecHost;

    @Column(name = "applcm_host")
    private String applcmHost;

    @Column(name = "operational_status")
    private String operationalStatus;

    @Column(name = "operation_info")
    private String operationInfo;

    @Column(name = "create_time")
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;
}