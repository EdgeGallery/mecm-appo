/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package org.edgegallery.mecm.appo.bpmn.tasks;

import static org.edgegallery.mecm.appo.bpmn.tasks.Apm.FAILED_TO_LOAD_YAML;
import static org.edgegallery.mecm.appo.bpmn.tasks.Apm.FAILED_TO_UNZIP_CSAR;
import static org.edgegallery.mecm.appo.utils.AppoServiceHelper.isFileWithSuffixExist;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppPackageMf;
import org.edgegallery.mecm.appo.model.AppRule;
import org.edgegallery.mecm.appo.model.AppServiceRequired;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.utils.AppoServiceHelper;
import org.edgegallery.mecm.appo.utils.Constants;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Check before deploymentappIs there a corresponding instance of the package dependency.
 *
 * @author 21cn/cuijch
 * @date 2020/11/9
 */
public class DeComposeAppPkgTask extends ProcessflowAbstractTask {

    private static final String OPERATIONAL_STATUS_INSTANTIATED = "Instantiated";
    private static final String YAML_KEY_TOPOLOGY = "topology_template";
    private static final String YAML_KEY_NODES = "node_templates";
    private static final String YAML_KEY_APP_CONFIG = "app_configuration";
    private static final String YAML_KEY_PROPERTIES = "properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeComposeAppPkgTask.class);
    private final DelegateExecution execution;
    private final String appPkgBasePath;
    private final AppInstanceInfoService appInstanceInfoService;

    /**
     * Constructor.
     *
     * @param delegateExecution      Execution object
     * @param appPkgBasePath         Package path
     * @param appInstanceInfoService Application case information
     */
    public DeComposeAppPkgTask(DelegateExecution delegateExecution, String appPkgBasePath,
                               AppInstanceInfoService appInstanceInfoService) {
        this.execution = delegateExecution;
        this.appPkgBasePath = appPkgBasePath;
        this.appInstanceInfoService = appInstanceInfoService;
    }

    private AppPackageMf getDeploymentType(String appPkgDir) {
        List<File> files = (List<File>) FileUtils.listFiles(new File(appPkgDir), null, true);
        for (File file: files) {
            if (isFileWithSuffixExist(file.getName(), ".mf")) {
                try (InputStream inputStream = new FileInputStream(file)) {
                    Yaml yaml = new Yaml(new SafeConstructor());
                    Map<String, Object> mfMap = yaml.load(inputStream);

                    AppPackageMf mf = new AppPackageMf();
                    mf.setAppClass(mfMap.get("app_class").toString());
                    return mf;
                } catch (IOException e) {
                    throw new AppoException("failed to read .mf from app package");
                }
            }
        }
        throw new AppoException("failed, .mf file not available in app package");
    }

    private String getEntryDefinitionFromMetadata(String appPkgDir) {
        List<File> files = (List<File>) FileUtils.listFiles(new File(appPkgDir), null, true);
        for (File file: files) {
            if (isFileWithSuffixExist(file.getName(), ".meta")) {
                try (InputStream inputStream = new FileInputStream(file)) {
                    Yaml yaml = new Yaml(new SafeConstructor());
                    Map<String, Object> meatData = yaml.load(inputStream);
                    return meatData.get("Entry-Definitions").toString();
                } catch (IOException e) {
                    throw new AppoException("failed to read metadata from app package");
                }
            }
        }
        throw new AppoException("failed, main service yaml not available in app package");
    }

    /**
     * Executor to decompose application package.
     */
    public void execute() {
        LOGGER.info("Decompose application package...");

        final String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        final String appInstanceId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
        final String appPackageId = (String) execution.getVariable(Constants.APP_PACKAGE_ID);
        final String appId = (String) execution.getVariable(Constants.APP_ID);
        final String mecHost = (String) execution.getVariable(Constants.MEC_HOST);

        LOGGER.info("param: tenant:{}, app_instance_id:{}, app_package_id:{}, app_id:{}, mec_host:{}", tenantId,
                appInstanceId, appPackageId, appId, mecHost);

        final String appPackagePath = appPkgBasePath + appInstanceId + Constants.SLASH + appPackageId
                + Constants.APP_PKG_EXT;

        String appPkgDir = FilenameUtils.removeExtension(appPackagePath);
        try {
            AppoServiceHelper.unzipApplicationPacakge(appPackagePath, appPkgDir);

            String mainServiceYaml = appPkgDir + "/" + getEntryDefinitionFromMetadata(appPkgDir);

            AppPackageMf mf = getDeploymentType(appPkgDir);
            String appDefnDir = FilenameUtils.removeExtension(mainServiceYaml);
            AppoServiceHelper.unzipApplicationPacakge(mainServiceYaml, appDefnDir);

            mainServiceYaml = appDefnDir + "/" + getEntryDefinitionFromMetadata(appDefnDir);

            Map<String, Object> mainTemplateMap = loadMainServiceTemplateYaml(mainServiceYaml);
            updateApplicationDescriptor(mainTemplateMap);

            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

            FileUtils.deleteDirectory(new File(appPkgDir));
        } catch (AppoException ex) {
            LOGGER.error(ex.getMessage());
            setProcessflowExceptionResponseAttributes(execution, ex.getMessage(), Constants.PROCESS_FLOW_ERROR);
        } catch (IOException ex) {
            LOGGER.error("failed to delete package directory");
        }
    }

    private Map<String, Object> loadMainServiceTemplateYaml(String yamlFile) {
        Map<String, Object> mainTemplateMap;
        Yaml yaml = new Yaml(new SafeConstructor());
        try (InputStream inputStream = new FileInputStream(new File(yamlFile))) {
            mainTemplateMap = yaml.load(inputStream);
        } catch (YAMLException | FileNotFoundException e) {
            LOGGER.error(FAILED_TO_LOAD_YAML);
            throw new AppoException(FAILED_TO_LOAD_YAML);
        } catch (IOException e) {
            throw new AppoException(FAILED_TO_LOAD_YAML);
        }
        return mainTemplateMap;
    }

    /**
     * Updates application descriptor from main service template yaml to the exection environment.
     *
     * @param mainTemplateMap main template map
     */
    public void updateApplicationDescriptor(Map<String, Object> mainTemplateMap) {
        Map<String, Object> topology = (Map<String, Object>) mainTemplateMap.get(YAML_KEY_TOPOLOGY);
        if (topology == null) {
            LOGGER.error("topology template null in main service template yaml");
            return;
        }

        Map<String, Object> nodes = (Map<String, Object>) topology.get(YAML_KEY_NODES);
        if (nodes == null) {
            LOGGER.error("nodes null in main service template yaml");
            return;
        }
        Map<String, Object> appConfigs = (Map<String, Object>) nodes.get(YAML_KEY_APP_CONFIG);
        if (appConfigs == null) {
            LOGGER.error("appConfigs null in main service template yaml");
            return;
        }
        Map<String, Object> properties = (Map<String, Object>) appConfigs.get(YAML_KEY_PROPERTIES);
        if (properties == null) {
            LOGGER.error("properties null in main service template yaml");
            return;
        }

        ModelMapper mapper = new ModelMapper();
        AppRule appRule = mapper.map(properties, AppRule.class);

        checkMainTemplate(appRule);

        if (appRule.getAppTrafficRule() == null && appRule.getAppDNSRule() == null) {
            return;
        }

        //Get app name from the input not from the package app name
        String appName = (String) execution.getVariable(Constants.APP_NAME);
        appRule.setAppName(appName);

        Gson gson = new Gson();
        String appRulejson = gson.toJson(appRule);
        execution.setVariable(Constants.APP_RULES, appRulejson);

        LOGGER.info("Set app rules : {}", appRulejson);
    }

    /**
     * FromappRuleGet the list of dependencies in，If not empty，Check if dependencies are deployed.
     *
     * @param appRule Include dependency list
     */
    public void checkMainTemplate(AppRule appRule) {
        if (appRule.getAppServiceRequired() == null) {
            return;
        }
        String tenantId = (String) execution.getVariable(Constants.TENANT_ID);
        String appInstanceId = (String) execution.getVariable(Constants.APP_INSTANCE_ID);
        String mecHost = (String) execution.getVariable(Constants.MEC_HOST);

        /* according tomec hostFilter the list of instances，according toappPkgIdConverted tomap，
         Filter out non-statusactiveInstance of */
        List<AppInstanceInfo> appInstanceInfoListInHost = appInstanceInfoService
                .getAppInstanceInfoByMecHost(tenantId, mecHost);

        LOGGER.debug("app instance in mec host: {}, number:{}", mecHost, appInstanceInfoListInHost.size());

        Map<String, AppInstanceInfo> appInstanceInfoMapWithPkg = appInstanceInfoListInHost.stream()
                .filter(appInstanceInfo -> OPERATIONAL_STATUS_INSTANTIATED
                        .equals(appInstanceInfo.getOperationalStatus()))
                .collect(Collectors.toMap(AppInstanceInfo::getAppId, appInstanceInfo -> appInstanceInfo));

        Gson gson = new Gson();
        List<AppInstanceDependency> dependencies = new ArrayList<>();

        // ParsingMainServiceTemplate.yaml，Confirmed dependentAPPWhether to be deployed
        for (AppServiceRequired required : appRule.getAppServiceRequired()) {
            // For platform services，does not existpackageId，No need to check
            if (null == required.getPackageId() || "".equals(required.getPackageId())) {
                continue;
            }
            AppInstanceInfo appInstanceInfo = appInstanceInfoMapWithPkg.get(required.getAppId());
            if (appInstanceInfo == null) {
                throw new AppoException("dependency app " + required.getSerName() + " not deployed");
            }

            AppInstanceDependency dependency = new AppInstanceDependency();
            dependency.setTenant(tenantId);
            dependency.setAppInstanceId(appInstanceId);
            dependency.setDependencyAppInstanceId(appInstanceInfo.getAppInstanceId());
            dependencies.add(dependency);
        }

        String appRequiredJson = gson.toJson(dependencies);
        execution.setVariable(Constants.APP_REQUIRED, appRequiredJson);
        LOGGER.info("Set app dependencies : {}", appRequiredJson);

        // becauseappServiceRequiredHas been serialized and stored inConstants.APP_REQUIREDUp
        // And behindAppRuleWill also be serialized and stored inConstants.APP_RULES
        // So putappServiceRequiredSet asnull
        appRule.setAppServiceRequired(null);
    }
}
