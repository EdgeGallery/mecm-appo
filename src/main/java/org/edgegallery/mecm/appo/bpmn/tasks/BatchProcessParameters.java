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

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.edgegallery.mecm.appo.apihandler.dto.BatchInstancesReqParam;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchProcessParameters implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessParameters.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        String action = (String) delegateExecution.getVariable("requestAction");

        switch (action) {
            case "SetAppCreateParams":
                setApplicationCreateParameters(delegateExecution);
                break;
            case "SetAppInstantiateParams":
                setApplicationInstantiateParameters(delegateExecution);
                break;
            case "SetAppTerminateParams":
                setApplicationTerminateParameters(delegateExecution);
                break;
            case "SetRequestCount":
                setAppRequestCount(delegateExecution);
                break;
            default:
                // Statements
        }
    }

    /**
     * Sets application parameters request input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void setApplicationCreateParameters(DelegateExecution delegateExecution) {
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);

        String mecHosts = (String) delegateExecution.getVariable(Constants.MEC_HOSTS);
        List<String> hosts = Arrays.asList(mecHosts.split(",", -1));

        String appInstIds = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_IDS);
        List<String> appInstanceIds = Arrays.asList(appInstIds.split(",", -1));

        Integer cnt = (Integer) delegateExecution.getVariable(Constants.APP_REQ_CNT);
        if (delegateExecution.getVariable(Constants.APP_REQ_CNT) == null) {
            cnt = hosts.size();
            delegateExecution.setVariable(Constants.APP_REQ_CNT, cnt);
        }

        if (cnt > 0) {
            delegateExecution.setVariable(Constants.MEC_HOST, hosts.get(cnt - 1));
            delegateExecution.setVariable(Constants.APP_INSTANCE_ID, appInstanceIds.get(cnt - 1));

            LOGGER.info("tenant_id: {}, mec_host: {}, app_instance_id: {}", tenantId, hosts.get(cnt - 1),
                    appInstanceIds.get(cnt - 1));
        }
    }

    /**
     * Sets application instantiate parameters request input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void setApplicationInstantiateParameters(DelegateExecution delegateExecution) {
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);

        String batchInstantiateParams = (String) delegateExecution.getVariable(Constants.BATCH_INSTANTIATION_PARAMS);
        BatchInstancesReqParam batchInstancesReqParam = new Gson().fromJson(batchInstantiateParams,
                BatchInstancesReqParam.class);

        Integer cnt = (Integer) delegateExecution.getVariable(Constants.APP_REQ_CNT);
        if (delegateExecution.getVariable(Constants.APP_REQ_CNT) == null) {
            cnt = batchInstancesReqParam.getInstantiationParameters().size();
            delegateExecution.setVariable(Constants.APP_REQ_CNT, cnt);
        }

        if (cnt > 0) {
            String appInstanceId = batchInstancesReqParam.getInstantiationParameters().get(cnt - 1).getAppInstanceId();
            delegateExecution.setVariable(Constants.APP_INSTANCE_ID, appInstanceId);

            String parameters = new Gson().toJson(batchInstancesReqParam
                    .getInstantiationParameters().get(cnt - 1).getParameters());
            delegateExecution.setVariable(Constants.INSTANTIATION_PARAMS, parameters);

            LOGGER.info("tenant_id: {}, app_instance_id: {}", tenantId, appInstanceId);
        }
    }

    /**
     * Sets application terminate parameters request input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void setApplicationTerminateParameters(DelegateExecution delegateExecution) {
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);

        String appInstIds = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_IDS);
        List<String> appInstanceIds = Arrays.asList(appInstIds.split(",", -1));

        Integer cnt = (Integer) delegateExecution.getVariable(Constants.APP_REQ_CNT);
        if (delegateExecution.getVariable(Constants.APP_REQ_CNT) == null) {
            cnt = appInstanceIds.size();
            delegateExecution.setVariable(Constants.APP_REQ_CNT, cnt);
        }

        if (cnt > 0) {
            delegateExecution.setVariable(Constants.APP_INSTANCE_ID, appInstanceIds.get(cnt - 1));

            LOGGER.info("tenant_id: {}, app_instance_id: {}", tenantId, appInstanceIds.get(cnt - 1));
        }
    }

    /**
     * Sets application request count.
     *
     * @param delegateExecution delegate execution
     */
    void setAppRequestCount(DelegateExecution delegateExecution) {
        Integer appReqCount = (Integer) delegateExecution.getVariable(Constants.APP_REQ_CNT);
        if (appReqCount > 0) {
            appReqCount = appReqCount - 1;
            delegateExecution.setVariable(Constants.APP_REQ_CNT, appReqCount);
        }
        LOGGER.info("app_req_cnt: {}", appReqCount);
    }
}
