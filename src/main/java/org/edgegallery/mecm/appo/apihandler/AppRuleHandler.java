package org.edgegallery.mecm.appo.apihandler;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.edgegallery.mecm.appo.apihandler.dto.AppRuleConfigDto;
import org.edgegallery.mecm.appo.model.AppRule;
import org.edgegallery.mecm.appo.service.AppoService;
import org.edgegallery.mecm.appo.utils.AppoResponse;
import org.edgegallery.mecm.appo.utils.Constants;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application Rule API handler.
 */
@Api(value = "Appo application eule api system")
@Validated
@RequestMapping("/appo/v1")
@RestController
public class AppRuleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppRuleHandler.class);

    private final AppoService appoService;

    @Autowired
    public AppRuleHandler(AppoService appoService) {
        this.appoService = appoService;
    }

    /**
     * Configures application rules for a given tenant and app instance ID.
     *
     * @param tenantId         tenant ID
     * @param appInstanceId    appInstance ID
     * @param appRuleConfigDto appDRule record details
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Configure application rules", response = String.class)
    @PostMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}/appd_configuration", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT')")
    public ResponseEntity<AppoResponse> addAppRules(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant identifier") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "app instance identifier") @PathVariable("app_instance_id")
            @Pattern(regexp = Constants.APP_INST_ID_REGX) @Size(max = 64) String appInstanceId,
            @Valid @ApiParam(value = "appD rule inventory information")
            @RequestBody AppRuleConfigDto appRuleConfigDto) {

        ModelMapper mapper = new ModelMapper();
        AppRule appRule = mapper.map(appRuleConfigDto, AppRule.class);

        return appoService.configureAppRules(accessToken, tenantId, appInstanceId, appRule, "POST");
    }

    /**
     * Updates application rules matching the given tenant ID & app instance ID.
     *
     * @param tenantId         tenant ID
     * @param appInstanceId    app instance Id
     * @param appRuleConfigDto appDRule record details
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Updates application rules", response = String.class)
    @PutMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}/appd_configuration",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT')")
    public ResponseEntity<AppoResponse> updateApplicationRules(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant identifier") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "app instance identifier") @PathVariable("app_instance_id")
            @Pattern(regexp = Constants.APP_INST_ID_REGX) @Size(max = 64) String appInstanceId,
            @Valid @ApiParam(value = "appD rule inventory information")
            @RequestBody AppRuleConfigDto appRuleConfigDto) {

        ModelMapper mapper = new ModelMapper();
        AppRule appRule = mapper.map(appRuleConfigDto, AppRule.class);

        return appoService.configureAppRules(accessToken, tenantId, appInstanceId, appRule, "PUT");
    }

    /**
     * Deletes application rules for a given tenant and app instance.
     *
     * @param tenantId      tenant ID
     * @param appInstanceId app instance Id
     * @return status code 200 on success, error code on failure
     */
    @ApiOperation(value = "Deletes application rules", response = String.class)
    @DeleteMapping(path = "/tenants/{tenant_id}/app_instances/{app_instance_id}/appd_configuration",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MECM_TENANT')")
    public ResponseEntity<AppoResponse> deleteApplicationRules(
            @ApiParam(value = "access token") @RequestHeader("access_token") String accessToken,
            @ApiParam(value = "tenant identifier") @PathVariable("tenant_id")
            @Pattern(regexp = Constants.TENENT_ID_REGEX) @Size(max = 64) String tenantId,
            @ApiParam(value = "app instance identifier") @PathVariable("app_instance_id")
            @Pattern(regexp = Constants.APP_INST_ID_REGX) @Size(max = 64) String appInstanceId,
            @Valid @ApiParam(value = "app rule information") @RequestBody AppRuleConfigDto appRuleConfigDto) {

        ModelMapper mapper = new ModelMapper();
        AppRule appRule = mapper.map(appRuleConfigDto, AppRule.class);

        return appoService.configureAppRules(accessToken, tenantId, appInstanceId, appRule, "DELETE");
    }
}
