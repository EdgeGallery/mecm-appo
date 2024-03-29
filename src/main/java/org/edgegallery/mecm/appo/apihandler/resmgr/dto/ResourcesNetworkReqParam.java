
package org.edgegallery.mecm.appo.apihandler.resmgr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class ResourcesNetworkReqParam {
    private String name;
    private boolean shared;
    private Subnet subnet;
}

