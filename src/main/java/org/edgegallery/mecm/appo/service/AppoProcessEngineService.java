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

package org.edgegallery.mecm.appo.service;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.ProcessEngines;
import org.springframework.stereotype.Service;

/**
 * service class must be aware of process engines. currently "default" process engine supported.
 */
@Service
public class AppoProcessEngineService {

    private static final String PROCESS_ENGINE_NAME = "default";

    /**
     * Retrieves process engine name.
     *
     * @return the process engine name
     */
    public String getEngineName() {
        return PROCESS_ENGINE_NAME;
    }

    /**
     * Retrieves process engine services.
     *
     * @return process engine services
     */
    public ProcessEngineServices getEngineServices() {
        return ProcessEngines.getProcessEngine(getEngineName());
    }
}