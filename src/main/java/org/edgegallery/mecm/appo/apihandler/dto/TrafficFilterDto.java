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

import java.util.LinkedHashSet;
import java.util.Set;
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
 * Traffic Rule input request schema.
 */
@Validated
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TrafficFilterDto {

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.IP_CIRD_REGX) String> srcAddress = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.PORT_REGEX) String> srcPort = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.IP_CIRD_REGX) String> dstAddress = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.PORT_REGEX) String> dstPort = new LinkedHashSet<>();

    @Size(max = 64)
    private Set<String> protocol = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<String> tag = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.IP_CIRD_REGX) String> srcTunnelAddress = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.IP_CIRD_REGX) String> dstTunnelAddress = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.PORT_REGEX) String> srcTunnelPort = new LinkedHashSet<>();

    @Size(max = 16)
    private Set<@Pattern(regexp = Constants.PORT_REGEX) String> dstTunnelPort = new LinkedHashSet<>();

    private int qCI;

    private int dSCP;

    private int tC;
}
