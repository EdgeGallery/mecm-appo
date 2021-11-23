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
public final class ResourcesServerReqParam {
    private String name;
    private String flavor;
    private String image;
    private String imageRef;
    private String availabilityZone;
    private String userData;
    private Boolean configDrive;
    private List<SecurityGroup> securityGroups = new ArrayList<SecurityGroup>();
    private List<NetWork> netWork = new ArrayList<NetWork>();
}

