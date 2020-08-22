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

package org.edgegallery.mecm.appo.apihandler;

import static org.edgegallery.mecm.appo.common.Constants.APP_PKG_ID_REGX;
import static org.edgegallery.mecm.appo.common.Constants.HOST_IP_REGX;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * Create instance input schema.
 */
@Validated
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateParam {

    @Size(max = 50)
    private String requestId;

    @NotEmpty(message = "Package ID is mandatory")
    @Pattern(regexp = APP_PKG_ID_REGX)
    private String appPackageId;

    @NotEmpty(message = "Application name is mandatory")
    @Size(min = 1, max = 200)
    private String appName;

    @NotEmpty(message = "Application instance descriptor ID is mandatory")
    @Size(min = 1, max = 50)
    private String appdId;

    @NotEmpty(message = "Application instance descriptor is mandatory")
    @Size(min = 1, max = 500)
    private String appInstanceDescription;

    @NotEmpty(message = "MEC host is mandatory")
    @Pattern(regexp = HOST_IP_REGX)
    private String mecHost;
}
