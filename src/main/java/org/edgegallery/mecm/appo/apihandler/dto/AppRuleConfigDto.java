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

package org.edgegallery.mecm.appo.apihandler.dto;

import static org.edgegallery.mecm.appo.utils.Constants.APP_NAME_REGEX;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
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
 * Application Rule input request schema.
 */
@Validated
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public final class AppRuleConfigDto {

    @Size(max = 16)
    private Set<@Valid TrafficRuleDto> appTrafficRule = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Valid DnsRuleDto> appDNSRule = new LinkedHashSet<>();

    @NotEmpty(message = "Application name is mandatory")
    @Size(max = 128)
    @Pattern(regexp = APP_NAME_REGEX, message = "App name is invalid. It must start and end with alpha numeric "
            + "character and special characters allowed are hyphen and underscore.")
    private String appName;

    private Boolean appSupportMp1;
}
