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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.mecm.appo.utils.Constants;
import org.springframework.validation.annotation.Validated;

/**
 * DnsRule input request schema.
 */
@Validated
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DnsRuleDto {

    @Size(max = 128)
    private String dnsRuleId;

    @Size(max = 256)
    private String domainName;

    @Size(max = 8)
    private String ipAddressType;

    @NotEmpty(message = "IP address is mandatory")
    @Size(max = 18)
    @Pattern(regexp = Constants.IP_CIRD_REGX, message = "IP address is invalid")
    private String ipAddress;

    private int ttl;
}
