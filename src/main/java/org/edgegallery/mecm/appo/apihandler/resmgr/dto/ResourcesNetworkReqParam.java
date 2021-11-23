
package org.edgegallery.mecm.appo.apihandler.resmgr.dto;

import java.util.ArrayList;
import java.util.List;
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
    private Boolean adminStateUp;
    private String dnsDomain;
    private Integer mtu;
    private Boolean portSecurityEnabled;
    private String providerNetworkType;
    private String providerPhysicalNetwork;
    private Integer providerSegmentationId;
    private String qosPolicyId;
    private Boolean routerExternal;
    private List<Segment> segments = new ArrayList<Segment>();
    private Boolean shared;
    private Boolean vlanTransparent;
    private Boolean isDefault;
    private List<Subnet> subnets = new ArrayList<Subnet>();
}

