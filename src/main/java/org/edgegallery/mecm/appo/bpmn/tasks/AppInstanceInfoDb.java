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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.NoSuchElementException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceDependency;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.model.AppRuleTask;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInstanceInfoDb extends ProcessflowAbstractTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(AppInstanceInfoDb.class);

    private final AppInstanceInfoService appInstanceInfoService;
    private final DelegateExecution execution;
    private final String operationType;

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
     * Executes DB operations on app instance info table.
     */
    public void execute() {
        switch (operationType) {
            case "insert":
                insertAppInstanceinfo(execution);
                break;
            case "update":
                updateAppInstanceinfo(execution);
                break;
            case "get":
                getAppInstanceinfo(execution);
                break;
            case "delete":
                deleteAppInstanceinfo(execution);
                break;
            case "updateAppRuleTask":
                updateAppRuleTask(execution);
                break;
            case "getAppRuleTask":
                getAppRuleTask(execution);
                break;
            case "deleteAppRuleTask":
                deleteAppRuleTask(execution);
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
     * @param delegateExecution delegate execution*
     * @throws AppoException exception
     */
    private void insertAppInstanceinfo(DelegateExecution delegateExecution) {
        AppInstanceInfo appInstanceInfo = new AppInstanceInfo();
        try {
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            appInstanceInfo.setTenant(tenantId);
            appInstanceInfo.setAppInstanceId((String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID));
            appInstanceInfo.setMecHost((String) delegateExecution.getVariable(Constants.MEC_HOST));
            appInstanceInfo.setAppPackageId((String) delegateExecution.getVariable(Constants.APP_PACKAGE_ID));
            appInstanceInfo.setAppName((String) delegateExecution.getVariable(Constants.APP_NAME));
            appInstanceInfo.setAppId((String) delegateExecution.getVariable(Constants.APP_ID));
            appInstanceInfo.setAppDescriptor((String) delegateExecution.getVariable(Constants.APP_DESCR));
            appInstanceInfo.setOperationalStatus(Constants.OPER_STATUS_CREATING);
            appInstanceInfoService.createAppInstanceInfo(tenantId, appInstanceInfo);
            String dependenciesJson = (String) delegateExecution.getVariable(Constants.APP_REQUIRED);
            if (dependenciesJson != null) {
                Gson gson = new Gson();
                List<AppInstanceDependency> dependencies = gson.fromJson(dependenciesJson,
                        new TypeToken<List<AppInstanceDependency>>() {
                        }.getType());
                appInstanceInfoService.createAppInstanceDependencyInfo(tenantId, dependencies);
            }

            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

            LOGGER.info("App instance info record added ");
        } catch (AppoException e) {
            LOGGER.error("Failed to add app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to add app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Retrieves record from application instance info DB.
     *
     * @param delegateExecution delegate execution
     * @return app instance info record
     * @throws AppoException exception
     */
    private AppInstanceInfo getAppInstanceinfo(DelegateExecution delegateExecution) {
        AppInstanceInfo appInstanceInfo = null;
        try {
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            LOGGER.info("Get application instance info {}", appInstanceId);

            appInstanceInfo = appInstanceInfoService.getAppInstanceInfo(tenantId, appInstanceId);

            delegateExecution.setVariable("app_instance_info", appInstanceInfo);
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException e) {
            LOGGER.error("Failed to get app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to get app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
        return appInstanceInfo;
    }

    /**
     * Updates application instance info in DB.
     *
     * @param delegateExecution delegate execution
     * @throws AppoException exception
     */
    private void updateAppInstanceinfo(DelegateExecution delegateExecution) {
        AppInstanceInfo appInstanceInfo;
        try {

            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            appInstanceInfo = new AppInstanceInfo();
            appInstanceInfo.setAppInstanceId(appInstanceId);

            LOGGER.info("Update application instance info {}", appInstanceId);

            String mepmIp = (String) delegateExecution.getVariable(Constants.MEPM_IP);
            if (mepmIp != null) {
                appInstanceInfo.setMepmHost(mepmIp);
            }

            String operationalStatus = (String) delegateExecution.getVariable("operational_status");
            appInstanceInfo.setOperationalStatus(operationalStatus);

            String responseCode = (String) delegateExecution.getVariable(RESPONSE_CODE);
            if (responseCode != null) {
                int statusCode = Integer.parseInt(responseCode);
                if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                    String response = (String) delegateExecution.getVariable(ERROR_RESPONSE);
                    appInstanceInfo.setOperationInfo(response);
                } else {
                    String response = (String) delegateExecution.getVariable(RESPONSE);
                    appInstanceInfo.setOperationInfo(response);
                }
            }

            String tenantId = (String) delegateExecution.getVariable("tenant_id");

            appInstanceInfoService.updateAppInstanceInfo(tenantId, appInstanceInfo);
            //deal with the dependency for update instance in create flow
            String dependenciesJson = (String) delegateExecution.getVariable(Constants.APP_REQUIRED);
            if (dependenciesJson != null && Constants.OPER_STATUS_CREATED.equals(operationalStatus)) {
                Gson gson = new Gson();
                List<AppInstanceDependency> dependencies = gson.fromJson(dependenciesJson,
                        new TypeToken<List<AppInstanceDependency>>() {
                        }.getType());
                appInstanceInfoService.createAppInstanceDependencyInfo(tenantId, dependencies);
                LOGGER.info("Update the dependency {}", dependenciesJson);
            }
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException | NumberFormatException e) {
            LOGGER.error("Failed to update app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to update app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Deletes application instance info record from DB.
     *
     * @param delegateExecution delegate execution
     * @throws AppoException exception
     */
    private void deleteAppInstanceinfo(DelegateExecution delegateExecution) {
        try {
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            LOGGER.info("Delete application instance info {}", appInstanceId);

            appInstanceInfoService.deleteAppInstanceInfo(tenantId, appInstanceId);
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException e) {
            LOGGER.error("Failed to delete app instance info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to delete app instance info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Updates application rule task info in DB.
     *
     * @param delegateExecution delegate execution
     * @throws AppoException exception
     */
    private void updateAppRuleTask(DelegateExecution delegateExecution) {
        AppRuleTask appRuleTaskInfo;
        try {

            String appRuleTaskId = (String) delegateExecution.getVariable(Constants.APPRULE_TASK_ID);
            LOGGER.info("Update application rule task id {}", appRuleTaskId);

            String responseCode = (String) delegateExecution.getVariable(RESPONSE_CODE);
            if (responseCode == null || responseCode.isEmpty()) {
                String appRules = (String) delegateExecution.getVariable(Constants.APP_RULES);
                appRuleTaskInfo = new AppRuleTask();
                appRuleTaskInfo.setAppRules(appRules);
            } else {
                int statusCode = Integer.parseInt(responseCode);
                if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                    String response = (String) delegateExecution.getVariable(ERROR_RESPONSE);
                    appRuleTaskInfo = convertRspStrToAppRuleTaskObj(response, true);
                } else {
                    String response = (String) delegateExecution.getVariable(RESPONSE);
                    appRuleTaskInfo = convertRspStrToAppRuleTaskObj(response, false);
                }
            }
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);

            appRuleTaskInfo.setTenant(tenantId);
            appRuleTaskInfo.setAppRuleTaskId(appRuleTaskId);

            appInstanceInfoService.updateAppRuleTaskInfo(tenantId, appRuleTaskInfo);
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException | NumberFormatException e) {
            LOGGER.error("Failed to update app rule task info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to update app rule task info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private AppRuleTask convertRspStrToAppRuleTaskObj(String jsonString, boolean isErrResp) {
        Gson gson = new Gson();
        AppRuleTask appRuleTask;
        try {
            appRuleTask = gson.fromJson(jsonString, AppRuleTask.class);
        } catch (JsonParseException ex) {
            appRuleTask = new AppRuleTask();
            appRuleTask.setDetailed(jsonString);
        }

        if (isErrResp) {
            appRuleTask.setConfigResult("FAILURE");
        } else {
            appRuleTask.setConfigResult("SUCCESS");
        }
        return appRuleTask;
    }

    /**
     * Retrieves application rule task info from DB.
     *
     * @param delegateExecution delegate execution
     * @throws AppoException exception
     */
    private void getAppRuleTask(DelegateExecution delegateExecution) {

        AppRuleTask appRuleTaskInfo;
        try {
            String appRuleTaskId = (String) delegateExecution.getVariable(Constants.APPRULE_TASK_ID);
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);

            LOGGER.info("Get application rule task {} ", appRuleTaskId);

            appRuleTaskInfo = appInstanceInfoService.getAppRuleTaskInfo(tenantId, appRuleTaskId);
            delegateExecution.setVariable(Constants.APP_RULES, appRuleTaskInfo.getAppRules());

            delegateExecution.setVariable(Constants.APP_RULE_CFG_STATUS, appRuleTaskInfo.getConfigResult());

            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (NoSuchElementException ex) {
            setProcessflowExceptionResponseAttributes(delegateExecution, ex.getMessage(),
                    Constants.PROCESS_RECORD_NOT_FOUND);
        } catch (AppoException | NumberFormatException e) {
            LOGGER.error("Failed to get app rule task info {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to get app rule task info",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    /**
     * Deletes application rule task info from DB.
     *
     * @param delegateExecution delegate execution
     * @throws AppoException exception
     */
    private void deleteAppRuleTask(DelegateExecution delegateExecution) {
        try {
            String appRuleTaskId = (String) delegateExecution.getVariable(Constants.APPRULE_TASK_ID);
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);

            LOGGER.info("Get application rule task {} ", appRuleTaskId);

            appInstanceInfoService.deleteAppRuleTaskInfo(tenantId, appRuleTaskId);

            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (NoSuchElementException ex) {
            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
        } catch (AppoException | NumberFormatException e) {
            LOGGER.error("Failed to update app rule task info record {}", e.getMessage());
            setProcessflowExceptionResponseAttributes(delegateExecution, "Failed to update app rule task info record",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }
}
