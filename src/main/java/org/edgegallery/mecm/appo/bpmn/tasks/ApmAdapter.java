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
import org.edgegallery.mecm.appo.service.AppoRestClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApmAdapter implements JavaDelegate {

    @Autowired
    private AppoRestClientService appoRestClientService;

    @Value("${appo.endpoints.apm.end-point}")
    private String apmService;

    @Value("${appo.endpoints.apm.port}")
    private String apmServicePort;

    @Value("${appo.appPackages.path}")
    private String appPkgsBasePath;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String servicePort = apmService + ":" + apmServicePort;
        Apm apm = new Apm(delegateExecution, appPkgsBasePath, servicePort, appoRestClientService);
        apm.execute();
    }
}
