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
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.edgegallery.mecm.appo.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessflowResponse implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessflowResponse.class);

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String responseType = (String) delegateExecution.getVariable("responseType");
        String response = (String) delegateExecution.getVariable("Response");
        String responseCode = (String) delegateExecution.getVariable("ResponseCode");

        if (responseType.equals("success")) {
            LOGGER.info("Set success response....");

            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP_CODE, responseCode);
            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP, response);
        } else if (responseType.equals("failure")) {
            LOGGER.info("Set failure response....");

            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP_CODE, responseCode);
            delegateExecution.setVariable(Constants.PROCESS_FLOW_ERR_RESP, response);
        } else {
            LOGGER.info("Unknow oper type, exception....");
            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP_CODE, responseCode);
            delegateExecution.setVariable(Constants.PROCESS_FLOW_EXCEPTION, response);
        }
    }
}
