/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.mecm.appo.bpmn.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInstanceInfoDb extends ProcessflowAbstractTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(AppInstanceInfoDb.class);

    private AppInstanceInfoService appInstanceInfoService;
    private DelegateExecution execution;
    private String operationType;

    /**
     * Constructor for DB operations.
     *
     * @param delegateExecution      delegate executions
     * @param appInstanceInfoService app instance info serivce
     */
    public AppInstanceInfoDb(DelegateExecution delegateExecution, AppInstanceInfoService appInstanceInfoService) {
        this.execution = delegateExecution;
        this.appInstanceInfoService = appInstanceInfoService;
        operationType = (String) delegateExecution.getVariable("operationType");
    }

    /**
     * Executes DB operations.
     */
    public void execute() {
        switch (operationType) {
            case "insert":
                insert(execution);
                break;
            case "update":
                update(execution);
                break;
            case "get":
                get(execution);
                break;
            case "delete":
                delete(execution);
                break;
            default:
                LOGGER.info("Invalid DB action...{}", operationType);
                setProcessflowExceptionResponseAttributes(execution, "Invalid DB action",
                        Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Adds record into application instance info DB.
     *
     * @param delegateExecution delegate execution
     * @return app instance info record
     * @throws AppoException exception
     */
    private AppInstanceInfo insert(DelegateExecution delegateExecution) {
        AppInstanceInfo appInstanceInfo = new AppInstanceInfo();
        try {
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            appInstanceInfo.setTenant(tenantId);
            appInstanceInfo.setAppInstanceId((String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID));
            appInstanceInfo.setMecHost((String) delegateExecution.getVariable(Constants.MEC_HOST));
            appInstanceInfo.setAppPackageId((String) delegateExecution.getVariable(Constants.APP_PACKAGE_ID));
            appInstanceInfo.setAppName((String) delegateExecution.getVariable(Constants.APP_NAME));
            appInstanceInfo.setAppdId((String) delegateExecution.getVariable(Constants.APPD_ID));
            appInstanceInfo.setAppDescriptor((String) delegateExecution.getVariable(Constants.APP_DESCR));
            appInstanceInfo.setOperationalStatus("Creating");
            appInstanceInfo = appInstanceInfoService.createAppInstanceInfo(tenantId, appInstanceInfo);
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
            LOGGER.info("App instance info record added ");
        } catch (AppoException e) {
            LOGGER.info("Failed to add app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to add app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
        return appInstanceInfo;
    }

    /**
     * Retrieves record from application instance info DB.
     *
     * @param delegateExecution delegate execution
     * @return app instance info record
     * @throws AppoException exception
     */
    private AppInstanceInfo get(DelegateExecution delegateExecution) {
        AppInstanceInfo appInstanceInfo = null;
        try {
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);
            appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);

            ObjectValue dataValue = Variables.objectValue(appInstanceInfo)
                    .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                    .create();
            delegateExecution.setVariable("app_instance_info", dataValue);
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException e) {
            LOGGER.info("Failed to get app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to get app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
        return appInstanceInfo;
    }

    /**
     * Updates application instance info in DB.
     *
     * @param delegateExecution delegate execution
     * @return application instance info
     * @throws AppoException exception
     */
    private AppInstanceInfo update(DelegateExecution delegateExecution) {
        AppInstanceInfo appInstanceInfo = null;
        try {

            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            appInstanceInfo = new AppInstanceInfo();
            appInstanceInfo.setAppInstanceId(appInstanceId);

            String responseCode = (String) delegateExecution.getVariable(RESPONSE_CODE);

            String operationalStatus = (String) delegateExecution.getVariable("operational_status");
            appInstanceInfo.setOperationalStatus(operationalStatus);

            int statusCode = Integer.parseInt(responseCode);
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String response = (String) delegateExecution.getVariable(ERROR_RESPONSE);
                appInstanceInfo.setOperationInfo(response);
            } else {
                String response = (String) delegateExecution.getVariable(RESPONSE);
                appInstanceInfo.setOperationInfo(response);
            }

            String tenantId = (String) delegateExecution.getVariable("tenant_id");

            appInstanceInfoService.updateAppInstanceInfo(tenantId, appInstanceInfo);
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException | NumberFormatException e) {
            LOGGER.info("Failed to update app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to update app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
        return appInstanceInfo;
    }

    /**
     * Deletes application instance info record from DB.
     *
     * @param delegateExecution delegate execution
     * @throws AppoException exception
     */
    private void delete(DelegateExecution delegateExecution) {
        try {
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            appInstanceInfoService.deleteAppInstanceInfo(tenantId, appInstanceId);
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException e) {
            LOGGER.info("Failed to delete app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to delete app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }
}
