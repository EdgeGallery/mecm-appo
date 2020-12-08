package org.edgegallery.mecm.appo.bpmn.tasks;

import static org.edgegallery.mecm.appo.bpmn.tasks.Apm.FAILED_TO_LOAD_YAML;
import static org.edgegallery.mecm.appo.bpmn.tasks.Apm.FAILED_TO_UNZIP_CSAR;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * 部署前检查app包的依赖是否有对应实例.
 *
 * @author 21cn/cuijch
 * @date 2020/11/9
 */
public class DeComposeAppPkgTask extends ProcessflowAbstractTask {
    private final DelegateExecution execution;
    private final String appPkgBasePath;
    private final AppInstanceInfoService appInstanceInfoService;

    private static final String OPERATIONAL_STATUS_INSTANTIATED = "Instantiated";

    private static final String YAML_KEY_TOPOLOGY_TEMPLATE = "topology_template";
    private static final String YAML_KEY_NODE_TEMPLATES = "node_templates";
    private static final String YAML_KEY_TYPE = "type";
    private static final String YAML_KEY_PROPERTIES = "properties";
    private static final String YAML_VALUE_CONFIGURATION = "tosca.nodes.nfv.app.configuration";
    private static final String YAML_KEY_APP_SERVICE_REQUIRED = "appServiceRequired";
    private static final String YAML_KEY_PACKAGE_ID = "packageId";
    private static final String YAML_KEY_SERVICE_NAME = "serName";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeComposeAppPkgTask.class);

    /**
     * 构造函数.
     *
     * @param delegateExecution 执行对象
     * @param appPkgBasePath 包路径
     * @param appInstanceInfoService 应用实例信息
     */
    public DeComposeAppPkgTask(DelegateExecution delegateExecution, String appPkgBasePath,
        AppInstanceInfoService appInstanceInfoService) {
        this.execution = delegateExecution;
        this.appPkgBasePath = appPkgBasePath;
        this.appInstanceInfoService = appInstanceInfoService;
    }

    /**
     * 执行体.
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

        try {
            LOGGER.info("check application {} dependency", appId);
            List<AppInstanceInfo> dependencies = getDependenciesAppInstance(appPackagePath, tenantId, mecHost);
            if (dependencies == null) {
                setProcessflowExceptionResponseAttributes(execution, "dependency APP not deployed",
                    Constants.PROCESS_FLOW_ERROR);
                return;
            }
            if (dependencies.size() > 0) {
                List<AppInstanceDependency> dependencyReqList = new ArrayList<>(dependencies.size());
                dependencies.forEach(item -> {
                    AppInstanceDependency appInstanceDependency = new AppInstanceDependency();
                    appInstanceDependency.setAppInstanceId(appInstanceId);
                    appInstanceDependency.setDependencyAppInstanceId(item.getAppInstanceId());
                    dependencyReqList.add(appInstanceDependency);
                });
                appInstanceInfoService.createAppInstanceDependencies(tenantId, dependencyReqList);
            }

            setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(execution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * 获取依赖的app实例列表.
     *
     * @param appPackagePath app package path
     * @param tenantId tenant ID
     * @param mecHost mec host
     * @return app实例列表
     */
    private List<AppInstanceInfo> getDependenciesAppInstance(String appPackagePath, String tenantId, String mecHost) {
        // 根据mec host筛选实例列表，按照appPkgId转化为map，过滤掉状态非active的实例
        List<AppInstanceInfo> appInstanceInfoListInHost = appInstanceInfoService
            .getAppInstanceInfoByMecHost(tenantId, mecHost);

        LOGGER.debug("app instance in mec host: {}, number:{}", mecHost, appInstanceInfoListInHost.size());

        Map<String, AppInstanceInfo> appInstanceInfoMapWithPkg = appInstanceInfoListInHost.stream()
            .filter(appInstanceInfo -> OPERATIONAL_STATUS_INSTANTIATED.equals(appInstanceInfo.getOperationalStatus()))
            .collect(Collectors.toMap(AppInstanceInfo::getAppPackageId, appInstanceInfo -> appInstanceInfo));

        List<Map<String, String>> dependencies = null;
        // 从csar中读取MainServiceTemplate.yaml
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(appPackagePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("Definitions/MainServiceTemplate.yaml")) {
                    Yaml yaml = new Yaml();
                    try {
                        Map<String, Object> mainTemplateMap = yaml.load(zis);
                        Map<String, Object> nodeTemplates = (Map<String, Object>) mainTemplateMap.get(YAML_KEY_TOPOLOGY_TEMPLATE);
                        Map<String, Object> appConfigurationProperties = null;
                        int appConfigurationNum = 0;
                        for (String key: nodeTemplates.keySet()) {
                            Map<String, Object> nodeTemplate = (Map<String, Object>) nodeTemplates.get(key);
                            if (nodeTemplate.containsKey(YAML_KEY_TYPE)
                                    && nodeTemplate.get(YAML_KEY_TYPE).equals(YAML_VALUE_CONFIGURATION)) {
                                appConfigurationProperties = (Map<String, Object>) nodeTemplate.get(YAML_KEY_PROPERTIES);
                                appConfigurationNum++;
                            }
                        }
                        if (appConfigurationNum > 1) {
                            LOGGER.error("analyze tosca template error");
                            throw new AppoException(FAILED_TO_LOAD_YAML);
                        }
                        if (appConfigurationProperties != null) {
                            dependencies = (List<Map<String, String>>) appConfigurationProperties.get(YAML_KEY_APP_SERVICE_REQUIRED);
                        }
                    } catch (Exception e) {
                        LOGGER.error(FAILED_TO_LOAD_YAML);
                        throw new AppoException(FAILED_TO_LOAD_YAML);
                    }
                    break;
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.error(FAILED_TO_UNZIP_CSAR);
            throw new AppoException(FAILED_TO_UNZIP_CSAR);
        }

        List<AppInstanceInfo> returnList = new ArrayList<>(10);

        if (dependencies == null) {
            return returnList;
        }

        // 解析MainServiceTemplate.yaml，确认依赖的APP是否被部署
        boolean dependencyExisted = true;
        StringBuilder noExistDependencyList = new StringBuilder();
        for (Map<String, String> dependency : dependencies) {
            String appPkgId = dependency.get(YAML_KEY_PACKAGE_ID);
            AppInstanceInfo appInstanceInfo = appInstanceInfoMapWithPkg.get(appPkgId);
            if (appInstanceInfo == null) {
                dependencyExisted = false;
                noExistDependencyList.append(dependency.get(YAML_KEY_SERVICE_NAME));
                noExistDependencyList.append(" ");
            } else {
                returnList.add(appInstanceInfo);
            }
        }

        if (dependencyExisted) {
            return returnList;
        } else {
            LOGGER.debug("dependency app {}not exist", noExistDependencyList.toString());
            return null;
        }
    }
}
