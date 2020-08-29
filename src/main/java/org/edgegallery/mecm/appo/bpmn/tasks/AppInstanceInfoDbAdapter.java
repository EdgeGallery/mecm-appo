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
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.service.AppInstanceInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppInstanceInfoDbAdapter implements JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(AppInstanceInfoDbAdapter.class);

    @Autowired
    private AppInstanceInfoService appInstanceInfoService;

    @Override
    public void execute(DelegateExecution delegateExecution) {

        AppInstanceInfoDb appInstanceInfoDb = new AppInstanceInfoDb(delegateExecution, appInstanceInfoService);
        try {
            appInstanceInfoDb.execute();
        } catch (AppoException e) {
            logger.debug("Failed to perform operation on DB {}", e.getMessage());
        }
    }
}
