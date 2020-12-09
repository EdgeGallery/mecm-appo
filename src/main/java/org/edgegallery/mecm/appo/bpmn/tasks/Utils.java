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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.edgegallery.mecm.appo.model.AppRule;
import org.edgegallery.mecm.appo.model.DnsRule;
import org.edgegallery.mecm.appo.model.TrafficRule;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class Utils extends ProcessflowAbstractTask implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Match hardware capabilities.
     *
     * @param execution execution variable
     */
    public void matchHwCapabilities(DelegateExecution execution) {

        String inHwCapabilities = (String) execution.getVariable(Constants.HW_CAPABILITIES);

        if (inHwCapabilities.isEmpty()) {
            return;
        }
        List<String> inputCapList = Arrays.asList(inHwCapabilities.split(",", -1));
        String hostHwCapabilityList = (String) execution.getVariable("hw_capabilities_list");
        List<String> hostCapList = Arrays.asList(hostHwCapabilityList.split(",", -1));

        for (String inCap : inputCapList) {
            boolean match = false;
            for (String hostCap : hostCapList) {
                if (inCap.equals(hostCap)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                LOGGER.info("Hardware capability match failed...input capability {}, host capability {}",
                        inHwCapabilities, hostHwCapabilityList);
                execution.setVariable("capabilityMatched", false);
                setProcessflowExceptionResponseAttributes(execution, "Hardware capability match failed",
                        Constants.PROCESS_FLOW_ERROR);
                break;
            }
        }
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String utilType = (String) execution.getVariable("utilType");
        switch (utilType) {
            case "matchCapabilities":
                matchHwCapabilities(execution);
                break;
            case "mergeAppRules":
                mergeAppRules(execution);
                break;
            default:
                LOGGER.info("Invalid util type...{}", utilType);
        }
    }

    /**
     * Match hardware capabilities.
     *
     * @param execution execution variable
     */
    public void mergeAppRules(DelegateExecution execution) {
        LOGGER.info("Merge input apprules with apprule exist from inventory...");
        String inAppRules = (String) execution.getVariable(Constants.APP_RULES);
        String inventoryAppRules = (String) execution.getVariable(Constants.INVENTORY_APP_RULES);

        if (StringUtils.isEmpty(inventoryAppRules)) {
            LOGGER.info("No app rules exists in inventory... configure apprule received from input {}", inAppRules);
            execution.setVariable(Constants.UPDATED_APP_RULES, inAppRules);
            return;
        }

        Gson gson = new Gson();
        AppRule inAppRule = gson.fromJson(inAppRules, AppRule.class);
        AppRule appRule = gson.fromJson(inventoryAppRules, AppRule.class);

        boolean isConfigure = false;
        String operType = (String) execution.getVariable(Constants.APP_RULE_ACTION);
        if (!"DELETE".equals(operType)) {
            isConfigure = true;
        }
        AppRule appRules = updateTrafficRule(appRule, inAppRule, isConfigure);
        appRules = updateDnsRule(appRules, inAppRule, isConfigure);

        String appRuleJson = gson.toJson(appRules);

        execution.setVariable(Constants.UPDATED_APP_RULES, appRuleJson);

        LOGGER.info("App rules:Input {}, \n existing {}, \nmerged {}", inAppRules, inventoryAppRules, appRuleJson);
    }

    /**
     * Updates traffic rule.
     *
     * @param appRule   existing application rule
     * @param inAppRule application rule from input
     * @return app rule
     */
    private AppRule updateTrafficRule(AppRule appRule, AppRule inAppRule, boolean isConfigure) {
        if (appRule == null || appRule.getAppTrafficRule() == null
                || inAppRule == null || inAppRule.getAppTrafficRule() == null) {
            LOGGER.error("App Rule or traffic rule is null");
            return appRule;
        }
        Set<TrafficRule> appTrafficRule = appRule.getAppTrafficRule();
        for (TrafficRule inTr : inAppRule.getAppTrafficRule()) {
            boolean matchFound = false;
            for (TrafficRule tr : appRule.getAppTrafficRule()) {
                if (tr.getTrafficRuleId().equals(inTr.getTrafficRuleId())) {
                    appTrafficRule.remove(tr);
                    if (isConfigure) {
                        appTrafficRule.add(inTr);
                        LOGGER.info("Updating traffic rule {}", inTr);
                    }
                    matchFound = true;
                    break;
                }
            }
            if (isConfigure && !matchFound) {
                //Add
                appTrafficRule.add(inTr);
                LOGGER.info("Adding traffic rule {}", inTr);
            }
        }
        appRule.setAppTrafficRule(appTrafficRule);
        return appRule;
    }

    /**
     * Updates dns rule.
     *
     * @param appRule   existing application rule
     * @param inAppRule application rule from input
     * @return app rule
     */
    private AppRule updateDnsRule(AppRule appRule, AppRule inAppRule, boolean isConfigure) {

        if (appRule == null || appRule.getAppDNSRule() == null
                || inAppRule == null || inAppRule.getAppDNSRule() == null) {
            LOGGER.error("App Rule or DNS rule is null");
            return appRule;
        }
        Set<DnsRule> appDnsRule = appRule.getAppDNSRule();
        for (DnsRule inDnsRule : inAppRule.getAppDNSRule()) {
            boolean matchFound = false;
            for (DnsRule dnsRule : appRule.getAppDNSRule()) {
                if (dnsRule.getDnsRuleId().equals(inDnsRule.getDnsRuleId())) {
                    appDnsRule.remove(dnsRule);
                    if (isConfigure) {
                        appDnsRule.add(inDnsRule);
                        LOGGER.info("Updating DNS rule {}", inDnsRule);
                    }
                    matchFound = true;
                    break;
                }
            }
            if (isConfigure && !matchFound) {
                //Add
                appDnsRule.add(inDnsRule);
                LOGGER.info("Adding DNS rule {}", inDnsRule);
            }
        }
        appRule.setAppDNSRule(appDnsRule);
        return appRule;
    }
}
