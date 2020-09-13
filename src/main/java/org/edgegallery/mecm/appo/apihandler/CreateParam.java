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

import static org.edgegallery.mecm.appo.utils.Constants.APPD_ID_REGEX;
import static org.edgegallery.mecm.appo.utils.Constants.APP_NAME_REGEX;
import static org.edgegallery.mecm.appo.utils.Constants.APP_PKG_ID_REGX;
import static org.edgegallery.mecm.appo.utils.Constants.HOST_IP_REGX;

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
public final class CreateParam {

    @NotEmpty(message = "Package ID is mandatory")
    @Size(max = 64)
    @Pattern(regexp = APP_PKG_ID_REGX)
    private String appPackageId;

    @NotEmpty(message = "Application name is mandatory")
    @Size(max = 128)
    @Pattern(regexp = APP_NAME_REGEX)
    private String appName;

    @NotEmpty(message = "Application instance ID is mandatory")
    @Size(max = 64)
    @Pattern(regexp = APPD_ID_REGEX)
    private String appId;

    @NotEmpty(message = "Application instance description is mandatory")
    @Size(max = 256)
    private String appInstanceDescription;

    @NotEmpty(message = "MEC host is mandatory")
    @Size(max = 15)
    @Pattern(regexp = HOST_IP_REGX)
    private String mecHost;
}
