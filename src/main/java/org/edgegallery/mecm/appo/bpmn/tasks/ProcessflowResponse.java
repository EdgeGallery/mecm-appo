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
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessflowResponse implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessflowResponse.class);

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String responseType = (String) delegateExecution.getVariable("responseType");

        String responseCode = (String) delegateExecution.getVariable(ProcessflowAbstractTask.RESPONSE_CODE);

        if (responseType.equals("success")) {
            String response = (String) delegateExecution.getVariable(ProcessflowAbstractTask.RESPONSE);

            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP_CODE, responseCode);
            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP, response);
            LOGGER.info("Set success, response: {}, response code: {} ", response, responseCode);
        } else if (responseType.equals("failure")) {
            String response = (String) delegateExecution.getVariable(ProcessflowAbstractTask.ERROR_RESPONSE);

            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP_CODE, responseCode);
            delegateExecution.setVariable(Constants.PROCESS_FLOW_ERR_RESP, response);
            LOGGER.info("Set failure, response: {}, response code: {} ", response, responseCode);
        } else {
            String response = (String) delegateExecution.getVariable(ProcessflowAbstractTask.FLOW_EXCEPTION);

            delegateExecution.setVariable(Constants.PROCESS_FLOW_RESP_CODE, responseCode);
            delegateExecution.setVariable(Constants.PROCESS_FLOW_EXCEPTION, response);
            LOGGER.info("Unknown, response: {}, response code: {} ", response, responseCode);
        }
    }
}
