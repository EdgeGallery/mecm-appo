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


import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            default:
                LOGGER.info("Invalid util type...{}", utilType);
        }
    }
}
