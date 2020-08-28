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

import java.util.Map;
import org.edgegallery.mecm.appo.common.AppoProcessFlowResponse;

public interface AppoProcessflowService {

    /**
     * Start processing process flow asychronously, process flow status can be obtained by querying app instance info
     * table using app instance ID.
     *
     * @param processKey   process key
     * @param requestInput input parameters
     */
    void executeProcessAsync(String processKey, Map<String, String> requestInput);

    /**
     * Start processing process flow sychronously.
     *
     * @param processKey   process key
     * @param requestInput input parameters
     * @return workflow response
     */
    AppoProcessFlowResponse executeProcessSync(String processKey, Map<String, String> requestInput);
}