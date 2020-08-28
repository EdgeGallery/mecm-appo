package org.edgegallery.mecm.appo.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.repository.AppInstanceInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppInstanceInfoServiceImpl implements AppInstanceInfoService {

    public static final Logger logger = LoggerFactory.getLogger(AppInstanceInfoServiceImpl.class);
    private static final String RECORD_NOT_FOUND = "Record does not exist with app instance id ";
    private AppInstanceInfoRepository appInstanceInfoRepository;

    @Autowired
    public AppInstanceInfoServiceImpl(AppInstanceInfoRepository appInstanceInfoRepository) {
        this.appInstanceInfoRepository = appInstanceInfoRepository;
    }

    @Override
    public AppInstanceInfo getAppInstanceInfo(String tenantId, String appInstanceId) {
        logger.debug("Get application instance {}... from DB", appInstanceId);

        Optional<AppInstanceInfo> info = appInstanceInfoRepository.findById(appInstanceId);
        if (!info.isPresent() || !info.get().getAppInstanceId().equals(appInstanceId)
                || !info.get().getTenant().equals(tenantId)) {
            throw new AppoException(RECORD_NOT_FOUND + appInstanceId);
        }
        return info.get();
    }

    @Override
    public List<AppInstanceInfo> getAllAppInstanceInfo(String tenantId) {
        logger.debug("Get all application instance info of tenant {} ... from DB", tenantId);

        List<AppInstanceInfo> tenantAppInstanceInfos = new LinkedList<>();
        appInstanceInfoRepository.findAll().forEach(appInstanceInfo -> {
            if (appInstanceInfo.getTenant().equals(tenantId)) {
                tenantAppInstanceInfos.add(appInstanceInfo);
            }
        });

        return tenantAppInstanceInfos;
    }

    @Override
    public AppInstanceInfo createAppInstanceInfo(String tenantId,
                                                 AppInstanceInfo appInstanceInfo) {
        logger.debug("Add application instance {}...to DB", appInstanceInfo.getAppInstanceId());

        appInstanceInfo.setTenant(tenantId);
        return appInstanceInfoRepository.save(appInstanceInfo);
    }

    @Override
    public void deleteAppInstanceInfo(String tenantId, String appInstanceId) {
        logger.debug("Delete application instance {}... from DB", appInstanceId);

        Optional<AppInstanceInfo> info = appInstanceInfoRepository.findById(appInstanceId);
        if (!info.isPresent() || !info.get().getAppInstanceId().equals(appInstanceId)
                || !info.get().getTenant().equals(tenantId)) {
            throw new AppoException(RECORD_NOT_FOUND + appInstanceId);
        }
        appInstanceInfoRepository.deleteById(appInstanceId);
    }

    @Override
    @Transactional
    public AppInstanceInfo updateAppInstanceInfo(String tenantId, AppInstanceInfo appInstanceInfo) {
        logger.debug("Update application instance {}... to DB", appInstanceInfo.getAppInstanceId());
        appInstanceInfo.setTenant(tenantId);

        Optional<AppInstanceInfo> info = appInstanceInfoRepository.findById(appInstanceInfo.getAppInstanceId());
        if (!info.isPresent() || !info.get().getAppInstanceId().equals(appInstanceInfo.getAppInstanceId())
                || !info.get().getTenant().equals(tenantId)) {
            throw new AppoException(RECORD_NOT_FOUND + appInstanceInfo.getAppInstanceId());
        }
        AppInstanceInfo dbAppInstanceInfo = info.get();

        dbAppInstanceInfo.setOperationalStatus(appInstanceInfo.getOperationalStatus());
        dbAppInstanceInfo.setOperationInfo(appInstanceInfo.getOperationInfo());

        dbAppInstanceInfo.setCreateTime(info.get().getCreateTime());
        return appInstanceInfoRepository.save(dbAppInstanceInfo);
    }
}
