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

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class DeComposeApplicationPackageTest {

    @InjectMocks
    DeComposeApplicationPackage deComposeApplicationPackage = new DeComposeApplicationPackage();

    @Mock
    ExecutionImpl execution;

    @Test
    public void testExcuteDeComposeApplicationPackage() throws Exception {
        deComposeApplicationPackage.execute(execution);
    }
}
