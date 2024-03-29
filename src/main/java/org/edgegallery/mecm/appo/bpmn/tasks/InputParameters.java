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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputParameters.class);

    private final String action;
    private final DelegateExecution delegateExecution;

    public InputParameters(DelegateExecution delegateExecution) {
        this.action = (String) delegateExecution.getVariable("requestAction");
        this.delegateExecution = delegateExecution;
    }

    /**
     * Sets request inputs to the delegate execution.
     */
    public void setInputParameters() {
        switch (action) {
            case "CreateAppInstance":
            case "BatchCreateAppInstance":
                createAppInstance(delegateExecution);
                break;
            case "BatchInstantiateAppInstance":
            case "BatchTerminateAppInstance":
                batchInstantiateAppInstance(delegateExecution);
                break;
            case "InstantiateAppInstance":
            case "TerminateAppInstance":
            case "QueryAppInstance":
            case "AppInstanceProfile":
                instantiateAppInstance(delegateExecution);
                break;
            case "QueryCapabilities":
            case "QueryKPI":
                queryCapabilities(delegateExecution);
                break;
            case "ConfigureAppRule":
                configAppRule(delegateExecution);
                break;
            default:
                // Statements
        }
    }

    /**
     * Sets create app instance input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void createAppInstance(DelegateExecution delegateExecution) {
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
        String appPkgId = (String) delegateExecution.getVariable(Constants.APP_PACKAGE_ID);
        String appDescr = (String) delegateExecution.getVariable(Constants.APP_DESCR);
        String appId = (String) delegateExecution.getVariable(Constants.APP_ID);
        String appName = (String) delegateExecution.getVariable(Constants.APP_NAME);
        String appRuleTaskId = (String) delegateExecution.getVariable(Constants.APPRULE_TASK_ID);
        String hwCapabilities = (String) delegateExecution.getVariable(Constants.HW_CAPABILITIES);

        delegateExecution.setVariable(Constants.ACCESS_TOKEN, accessToken);
        delegateExecution.setVariable(Constants.TENANT_ID, tenantId);
        delegateExecution.setVariable(Constants.APP_PACKAGE_ID, appPkgId);
        delegateExecution.setVariable(Constants.APP_DESCR, appDescr);
        delegateExecution.setVariable(Constants.APP_ID, appId);
        delegateExecution.setVariable(Constants.APP_NAME, appName);
        delegateExecution.setVariable(Constants.HW_CAPABILITIES, hwCapabilities);
        delegateExecution.setVariable(Constants.APPRULE_TASK_ID, appRuleTaskId);

        if ("CreateAppInstance".equals(action)) {
            String mecHost = (String) delegateExecution.getVariable(Constants.MEC_HOST);
            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);
            delegateExecution.setVariable(Constants.MEC_HOST, mecHost);
            delegateExecution.setVariable(Constants.APP_INSTANCE_ID, appInstanceId);

            LOGGER.info("tenant_id: {},app_package_id: {},mec_host: {},app_instance_description: {},app_id: {},"
                            + "app_name: {},app_instance_id: {}, hw_capabilities: {}", tenantId, appPkgId, mecHost,
                    appDescr, appId, appName, appInstanceId, hwCapabilities);
        } else if ("BatchCreateAppInstance".equals(action)) {
            String mecHosts = (String) delegateExecution.getVariable(Constants.MEC_HOSTS);
            String appInstanceIds = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_IDS);
            delegateExecution.setVariable(Constants.MEC_HOSTS, mecHosts);
            delegateExecution.setVariable(Constants.APP_INSTANCE_IDS, appInstanceIds);

            LOGGER.info("tenant_id: {},app_package_id: {},mec_hosts: {},app_instance_description: {},app_id: {},"
                            + "app_name: {},app_instance_ids: {}, hw_capabilities: {}", tenantId, appPkgId, mecHosts,
                    appDescr, appId, appName, appInstanceIds, hwCapabilities);
        }
    }

    /**
     * Sets create app instance input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void batchInstantiateAppInstance(DelegateExecution delegateExecution) {
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
        String appInstanceIds = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_IDS);

        delegateExecution.setVariable(Constants.ACCESS_TOKEN, accessToken);
        delegateExecution.setVariable(Constants.TENANT_ID, tenantId);
        delegateExecution.setVariable(Constants.APP_INSTANCE_IDS, appInstanceIds);
        String instantiationParams = (String) delegateExecution.getVariable(Constants.BATCH_INSTANTIATION_PARAMS);
        if (instantiationParams != null) {
            delegateExecution.setVariable(Constants.BATCH_INSTANTIATION_PARAMS, instantiationParams);
        }
        LOGGER.info("tenant_id: {}", tenantId);
    }

    /**
     * Sets instantiate app instance input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void instantiateAppInstance(DelegateExecution delegateExecution) {
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
        String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);
        String appRuletaskId = (String) delegateExecution.getVariable(Constants.APPRULE_TASK_ID);

        delegateExecution.setVariable(Constants.ACCESS_TOKEN, accessToken);
        delegateExecution.setVariable(Constants.TENANT_ID, tenantId);
        delegateExecution.setVariable(Constants.APP_INSTANCE_ID, appInstanceId);
        delegateExecution.setVariable(Constants.APPRULE_TASK_ID, appRuletaskId);
        String instantiationParams = (String) delegateExecution.getVariable(Constants.INSTANTIATION_PARAMS);
        if (instantiationParams != null) {
            delegateExecution.setVariable(Constants.INSTANTIATION_PARAMS, instantiationParams);
        }

        LOGGER.info("tenant_id: {}, app_instance_id: {}", tenantId, appInstanceId);
    }

    /**
     * Sets query capabilities request input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void queryCapabilities(DelegateExecution delegateExecution) {
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
        String hostIp = (String) delegateExecution.getVariable(Constants.MEC_HOST);
        String capabilityId = (String) delegateExecution.getVariable(Constants.MEP_CAPABILITY_ID);
        delegateExecution.setVariable(Constants.ACCESS_TOKEN, accessToken);
        delegateExecution.setVariable(Constants.TENANT_ID, tenantId);
        delegateExecution.setVariable(Constants.MEC_HOST, hostIp);
        delegateExecution.setVariable(Constants.MEP_CAPABILITY_ID, capabilityId);

        LOGGER.info("tenant_id: {}, mec_host: {}", tenantId, hostIp);
    }

    /**
     * Sets query capabilities request input parameters.
     *
     * @param delegateExecution delegate execution
     */
    void configAppRule(DelegateExecution delegateExecution) {
        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
        String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
        String appRules = (String) delegateExecution.getVariable(Constants.APP_RULES);
        String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);
        String appRuleAction = (String) delegateExecution.getVariable(Constants.APP_RULE_ACTION);
        String appRuleTaskId = (String) delegateExecution.getVariable(Constants.APPRULE_TASK_ID);
        delegateExecution.setVariable(Constants.ACCESS_TOKEN, accessToken);
        delegateExecution.setVariable(Constants.TENANT_ID, tenantId);
        delegateExecution.setVariable(Constants.APP_RULES, appRules);
        delegateExecution.setVariable(Constants.APP_INSTANCE_ID, appInstanceId);
        delegateExecution.setVariable(Constants.APP_RULE_ACTION, appRuleAction);
        delegateExecution.setVariable(Constants.APPRULE_TASK_ID, appRuleTaskId);

        LOGGER.info("tenant_id: {}, app_instance_id: {}", tenantId, appInstanceId);
    }
}
