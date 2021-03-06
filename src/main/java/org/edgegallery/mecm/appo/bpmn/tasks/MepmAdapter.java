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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MepmAdapter implements JavaDelegate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${server.ssl.enabled:false}")
    private String isSslEnabled;

    @Value("${appo.appPackages.path}")
    private String appPkgsBasePath;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Mepm mepm = new Mepm(delegateExecution, Boolean.parseBoolean(isSslEnabled), appPkgsBasePath, restTemplate);
        mepm.execute();
    }
}
