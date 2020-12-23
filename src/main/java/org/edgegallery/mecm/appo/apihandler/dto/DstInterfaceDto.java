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
 * Dst interface Inventory input request schema.
 */
@Validated
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DstInterfaceDto {

    @Size(max = 64)
    private String interfaceType;

    private TunnelInfoDto tunnelInfo;

    @Size(max = 17)
    @Pattern(regexp = Constants.MAC_ADDRESS_REGX, message = "Source MAC address is invalid")
    private String srcMacAddress;

    @Size(max = 17)
    @Pattern(regexp = Constants.MAC_ADDRESS_REGX, message = "Destination MAC address is invalid")
    private String dstMacAddress;

    @NotEmpty(message = "Destination ip address is mandatory")
    @Size(max = 15)
    @Pattern(regexp = Constants.IP_CIRD_REGX, message = "Destination ip address is invalid")
    private String dstIpAddress;
}
