package org.edgegallery.mecm.appo.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppoTenant;
import org.edgegallery.mecm.appo.repository.AppInstanceDependencyRepository;
import org.edgegallery.mecm.appo.repository.AppInstanceInfoRepository;
import org.edgegallery.mecm.appo.repository.AppoTenantRepository;
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
    private AppoTenantRepository appoTenantRepository;
    private AppInstanceDependencyRepository appInstanceDependencyRepository;

    /**
     *  构造函数.
     * @param appInstanceInfoRepository app实例信息仓库
     * @param appInstanceDependencyRepository 依赖的app实例仓库
     * @param appoTenantRepository 租户仓库
     */
    @Autowired
    public AppInstanceInfoServiceImpl(AppInstanceInfoRepository appInstanceInfoRepository,
        AppInstanceDependencyRepository appInstanceDependencyRepository, AppoTenantRepository appoTenantRepository) {
        this.appInstanceInfoRepository = appInstanceInfoRepository;
        this.appInstanceDependencyRepository = appInstanceDependencyRepository;
        this.appoTenantRepository = appoTenantRepository;
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
        if (dependencies.size() > 0) {
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
    public AppInstanceInfo updateAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo) {

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

        if (appInstanceInfo.getApplcmHost() != null) {
            info.setApplcmHost(appInstanceInfo.getApplcmHost());
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
        return appInstanceInfoRepository.save(info);
    }

    @Override
    public void createAppInstanceDependencies(String tenantId, List<AppInstanceDependency> appInstanceDependencies) {
        appInstanceDependencies.forEach(item -> {
            item.setTenant(tenantId);
            item.setId(UUID.randomUUID().toString());
        });
        appInstanceDependencyRepository.saveAll(appInstanceDependencies);
    }

    @Override
    public List<AppInstanceDependency> getDependenciesByDependencyAppInstanceId(String tenantId,
        String dependencyAppInstanceId) {
        return appInstanceDependencyRepository.getByDependencyAppInstanceId(tenantId, dependencyAppInstanceId);
    }
}
