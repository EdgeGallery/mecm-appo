package org.edgegallery.mecm.appo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Application Service Required input request schema.
 * @author 21cn/cuijch
 * @date 2020.12.23
 */
@Validated
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AppServiceRequired {
    private String serName;
    private String version;
    private Boolean requestedPermissions;
    private String appId;
    private String packageId;
}
