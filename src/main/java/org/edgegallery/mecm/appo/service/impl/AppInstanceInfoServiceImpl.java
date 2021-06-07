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


package org.edgegallery.mecm.appo.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppRuleTask;
import org.edgegallery.mecm.appo.model.AppoTenant;
import org.edgegallery.mecm.appo.repository.AppInstanceDependencyRepository;
import org.edgegallery.mecm.appo.repository.AppInstanceInfoRepository;
import org.edgegallery.mecm.appo.repository.AppRuleTaskRepository;
import org.edgegallery.mecm.appo.repository.AppoTenantRepository;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppInstanceInfoServiceImpl implements AppInstanceInfoService {

    public static final Logger LOGGER = LoggerFactory.getLogger(AppInstanceInfoServiceImpl.class);
    private static final String RECORD_NOT_FOUND = "Record does not exist, app instance id: ";
    private AppInstanceInfoRepository appInstanceInfoRepository;
    private AppRuleTaskRepository appRuleTaskRepository;
    private AppoTenantRepository appoTenantRepository;
    private AppInstanceDependencyRepository appInstanceDependencyRepository;

    /**
     * Constructor.
     *
     * @param appInstanceInfoRepository       appInstance information warehouse
     * @param appInstanceDependencyRepository dependentappInstance warehouse
     * @param appoTenantRepository            Tenant warehouse
     */
    @Autowired
    public AppInstanceInfoServiceImpl(AppInstanceInfoRepository appInstanceInfoRepository,
                                      AppInstanceDependencyRepository appInstanceDependencyRepository,
                                      AppoTenantRepository appoTenantRepository,
                                      AppRuleTaskRepository appRuleTaskRepository) {
        this.appInstanceInfoRepository = appInstanceInfoRepository;
        this.appInstanceDependencyRepository = appInstanceDependencyRepository;
        this.appoTenantRepository = appoTenantRepository;
        this.appRuleTaskRepository = appRuleTaskRepository;
    }

    @Override
    public AppInstanceInfo getAppInstanceInfo(String tenantId, String appInstanceId) {
        LOGGER.debug("Get application instance {}... from DB", appInstanceId);

        AppInstanceInfo info = appInstanceInfoRepository.findByTenantIdAndAppInstanceId(tenantId, appInstanceId);
        if (info == null) {
            LOGGER.debug("application instance info not found {}", appInstanceId);
            throw new NoSuchElementException(RECORD_NOT_FOUND + appInstanceId);
        }
        return info;
    }

    @Override
    public List<AppInstanceInfo> getAllAppInstanceInfo(String tenantId) {
        LOGGER.debug("Retrieving application instances of tenant {}", tenantId);

        return appInstanceInfoRepository.findByTenantId(tenantId);
    }

    @Override
    public List<AppInstanceInfo> getAppInstanceInfoByMecHost(String tenantId, String mecHost) {
        LOGGER.debug("Retrieving application instances of tenant {}, mec host {}", tenantId, mecHost);

        return appInstanceInfoRepository.findByTenantIdAndMecHost(tenantId, mecHost);
    }

    @Override
    public AppInstanceInfo createAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo) {

        LOGGER.debug("Add application instance {}", appInstanceInfo);
        boolean addTenant = false;
        AppInstanceInfo appInstance = null;
        appInstanceInfo.setTenant(tenantId);

        Optional<AppoTenant> info = appoTenantRepository.findById(tenantId);
        if (info.isPresent()) {
            List<AppInstanceInfo> record = appInstanceInfoRepository.findByTenantId(tenantId);
            if (record.size() == Constants.MAX_ENTRY_PER_TENANT_PER_MODEL) {
                LOGGER.error("Max app instance's limit {} reached", Constants.MAX_ENTRY_PER_TENANT_PER_MODEL);
                throw new AppoException(Constants.MAX_LIMIT_REACHED_ERROR);
            }
        } else {
            if (appoTenantRepository.count() == Constants.MAX_TENANTS) {
                LOGGER.error("Max tenant limit {} reached", Constants.MAX_TENANTS);
                throw new AppoException(Constants.MAX_LIMIT_REACHED_ERROR);
            }
            addTenant = true;
        }

        appInstance = appInstanceInfoRepository.save(appInstanceInfo);

        if (addTenant) {
            LOGGER.info("Add tenant {}", tenantId);
            AppoTenant tenant = new AppoTenant();
            tenant.setTenant(tenantId);
            appoTenantRepository.save(tenant);
        }
        return appInstance;
    }

    @Override
    @Transactional
    public void createAppInstanceDependencyInfo(String tenantId, List<AppInstanceDependency> dependencies) {
        if (!dependencies.isEmpty()) {
            dependencies.forEach(item -> {
                item.setTenant(tenantId);
                item.setId(UUID.randomUUID().toString());
            });
            appInstanceDependencyRepository.saveAll(dependencies);
        }
    }

    @Override
    @Transactional
    public void deleteAppInstanceInfo(String tenantId, String appInstanceId) {
        LOGGER.debug("Delete application instance {}... from DB", appInstanceId);

        AppInstanceInfo info = appInstanceInfoRepository.findByTenantIdAndAppInstanceId(tenantId, appInstanceId);
        if (info == null) {
            LOGGER.debug("Record does not exist, application instance {}", appInstanceId);
            throw new NoSuchElementException(RECORD_NOT_FOUND + appInstanceId);
        }

        appInstanceInfoRepository.deleteById(appInstanceId);
        List<AppInstanceDependency> dependencies = appInstanceDependencyRepository
                .getByAppInstanceId(tenantId, appInstanceId);
        if (!dependencies.isEmpty()) {
            appInstanceDependencyRepository.deleteAll(dependencies);
        }

        List<AppInstanceInfo> record = appInstanceInfoRepository.findByTenantId(tenantId);
        if (record.isEmpty()) {
            LOGGER.info("Delete tenant {}", tenantId);
            appoTenantRepository.deleteById(tenantId);
        }
    }

    @Override
    @Transactional
    public void updateAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo) {

        String appInstanceId = appInstanceInfo.getAppInstanceId();

        LOGGER.debug("Update application instance {}", appInstanceId);

        AppInstanceInfo info = appInstanceInfoRepository.findByTenantIdAndAppInstanceId(tenantId, appInstanceId);
        if (info == null) {
            LOGGER.debug("Record does not exist, application instance {}", appInstanceId);
            throw new NoSuchElementException(RECORD_NOT_FOUND + appInstanceId);
        }

        if (appInstanceInfo.getAppPackageId() != null) {
            info.setAppPackageId(appInstanceInfo.getAppPackageId());
        }

        if (appInstanceInfo.getAppName() != null) {
            info.setAppName(appInstanceInfo.getAppName());
        }

        if (appInstanceInfo.getAppId() != null) {
            info.setAppId(appInstanceInfo.getAppId());
        }

        if (appInstanceInfo.getAppDescriptor() != null) {
            info.setAppDescriptor(appInstanceInfo.getAppDescriptor());
        }

        if (appInstanceInfo.getMecHost() != null) {
            info.setMecHost(appInstanceInfo.getMecHost());
        }

        if (appInstanceInfo.getMepmHost() != null) {
            info.setMepmHost(appInstanceInfo.getMepmHost());
        }

        if (appInstanceInfo.getOperationalStatus() != null) {
            info.setOperationalStatus(appInstanceInfo.getOperationalStatus());
        }

        if (appInstanceInfo.getOperationInfo() != null) {
            String operInfo = appInstanceInfo.getOperationInfo();
            if (operInfo.length() > 250) {
                operInfo = operInfo.substring(0, 250) + "...";
            }
            info.setOperationInfo(operInfo);
        }
        LOGGER.debug("Update application instance {}", info);

        appInstanceInfoRepository.save(info);
    }

    @Override
    public List<AppInstanceDependency> getDependenciesByDependencyAppInstanceId(String tenantId,
                                                                                String dependencyAppInstanceId) {
        return appInstanceDependencyRepository.getByDependencyAppInstanceId(tenantId, dependencyAppInstanceId);
    }

    @Override
    public AppRuleTask getAppRuleTaskInfo(String tenantId, String appRuleTaskId) {
        LOGGER.debug("Get application rule task if {}... from DB", appRuleTaskId);

        AppRuleTask info = appRuleTaskRepository.findByTenantIdAndAppRuleTaskId(tenantId, appRuleTaskId);
        if (info == null) {
            LOGGER.debug("application rule task info not found {}", appRuleTaskId);
            throw new NoSuchElementException(RECORD_NOT_FOUND + appRuleTaskId);
        }
        return info;
    }

    @Override
    public AppRuleTask createAppRuleTaskInfo(String tenantId, AppRuleTask appRuleTaskInfo) {

        LOGGER.debug("Add application rule task info {}", appRuleTaskInfo);
        boolean addTenant = false;
        AppRuleTask appRuleTask = null;
        appRuleTaskInfo.setTenant(tenantId);

        Optional<AppoTenant> info = appoTenantRepository.findById(tenantId);
        if (info.isPresent()) {
            List<AppRuleTask> record = appRuleTaskRepository.findByTenantId(tenantId);
            if (record.size() == Constants.MAX_ENTRY_PER_TENANT_PER_MODEL) {
                LOGGER.error("Max app rule task's limit {} reached, delete old entry",
                        Constants.MAX_ENTRY_PER_TENANT_PER_MODEL);
                appRuleTaskRepository.deleteById(record.get(0).getAppRuleTaskId());
            }
        } else {
            if (appoTenantRepository.count() == Constants.MAX_TENANTS) {
                LOGGER.error("Max tenant limit {} reached", Constants.MAX_TENANTS);
                throw new AppoException(Constants.MAX_LIMIT_REACHED_ERROR);
            }
            addTenant = true;
        }

        appRuleTask = appRuleTaskRepository.save(appRuleTaskInfo);

        if (addTenant) {
            LOGGER.info("Add tenant {}", tenantId);
            AppoTenant tenant = new AppoTenant();
            tenant.setTenant(tenantId);
            appoTenantRepository.save(tenant);
        }
        return appRuleTask;
    }

    @Override
    @Transactional
    public void deleteAppRuleTaskInfo(String tenantId, String appRuleTaskId) {
        LOGGER.debug("Delete application rule task {}... from DB", appRuleTaskId);

        AppRuleTask info = appRuleTaskRepository.findByTenantIdAndAppRuleTaskId(tenantId, appRuleTaskId);
        if (info == null) {
            LOGGER.debug("Record does not exist, application rule task {}", appRuleTaskId);
            throw new NoSuchElementException(RECORD_NOT_FOUND + appRuleTaskId);
        }

        appRuleTaskRepository.deleteById(appRuleTaskId);

        List<AppRuleTask> appRuleTaskRec = appRuleTaskRepository.findByTenantId(tenantId);
        List<AppInstanceInfo> appInstanceRec = appInstanceInfoRepository.findByTenantId(tenantId);
        if (appRuleTaskRec.isEmpty() && appInstanceRec.isEmpty()) {
            LOGGER.info("Delete tenant {}", tenantId);
            appoTenantRepository.deleteById(tenantId);
        }
    }

    @Override
    @Transactional
    public AppRuleTask updateAppRuleTaskInfo(String tenantId, AppRuleTask appRuleTaskInfo) {

        String appRuleTaskId = appRuleTaskInfo.getAppRuleTaskId();

        LOGGER.debug("Update application rule task ID {}", appRuleTaskId);

        AppRuleTask info = appRuleTaskRepository.findByTenantIdAndAppRuleTaskId(tenantId, appRuleTaskId);
        if (info == null) {
            LOGGER.debug("Record does not exist, application rule task {}", appRuleTaskId);
            throw new NoSuchElementException(RECORD_NOT_FOUND + appRuleTaskId);
        }

        if (appRuleTaskInfo.getTaskId() != null) {
            info.setTaskId(appRuleTaskInfo.getTaskId());
        }

        if (appRuleTaskInfo.getAppInstanceId() != null) {
            info.setAppInstanceId(appRuleTaskInfo.getAppInstanceId());
        }

        if (appRuleTaskInfo.getAppRules() != null) {
            info.setAppRules(appRuleTaskInfo.getAppRules());
        }

        if (appRuleTaskInfo.getConfigResult() != null) {
            info.setConfigResult(appRuleTaskInfo.getConfigResult());
        }

        if (appRuleTaskInfo.getDetailed() != null) {
            String detail = appRuleTaskInfo.getDetailed();
            if (detail.length() > 250) {
                detail = detail.substring(0, 250) + "...";
            }
            info.setDetailed(detail);
        }

        LOGGER.debug("Update application rule task {}", info);
        return appRuleTaskRepository.save(info);
    }
}
