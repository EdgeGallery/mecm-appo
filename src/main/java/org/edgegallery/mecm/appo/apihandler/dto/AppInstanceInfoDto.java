package org.edgegallery.mecm.appo.apihandler.dto;

import static org.edgegallery.mecm.appo.utils.Constants.APP_INST_ID_REGX;
import static org.edgegallery.mecm.appo.utils.Constants.APP_PKG_ID_REGX;
import static org.edgegallery.mecm.appo.utils.Constants.TENENT_ID_REGEX;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
public final class AppInstanceInfoDto {

    @NotEmpty(message = "app instance ID is empty")
    @Pattern(regexp = APP_INST_ID_REGX)
    @Size(max = 64)
    private String appInstanceId;

    @NotEmpty(message = "app package ID is empty")
    @Pattern(regexp = APP_PKG_ID_REGX)
    @Size(max = 64)
    private String appPackageId;

    @NotEmpty(message = "tenant is empty")
    @Pattern(regexp = TENENT_ID_REGEX)
    @Size(max = 64)
    private String tenant;

    @NotEmpty(message = "app name is empty")
    @Size(max = 128)
    private String appName;

    @NotEmpty(message = "appd ID is empty")
    @Size(max = 64)
    private String appdId;

    @NotEmpty(message = "app descriptor is empty")
    @Size(max = 256)
    private String appDescriptor;

    @NotEmpty(message = "mec host is empty")
    @Size(max = 15)
    private String mecHost;

    @NotEmpty(message = "applcm host is empty")
    @Size(max = 15)
    private String applcmHost;

    @NotEmpty(message = "operational status is empty")
    @Size(max = 128)
    private String operationalStatus;

    @Size(max = 128)
    private String operationInfo;
    private String createTime;
    private String updateTime;
}
