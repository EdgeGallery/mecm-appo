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
import org.edgegallery.mecm.appo.utils.AppoTrustStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryAdapter implements JavaDelegate {

    @Value("${SSL_ENABLED:false}")
    private String isSslEnabled;

    @Value("${INVENTORY_ENDPOINT:mecm-inventory-service}")
    private String inventoryService;

    @Value("${INVENTORY_PORT:3201}")
    private String inventoryServicePort;

    @Value("${SSL_TRUST_STORE:}")
    private String trustStorePath;

    @Value("${SSL_TRUST_PASSWORD:}")
    private String trustStorePasswd;

    @Value("${USE_DEFAULT_TRUST_STORE:}")
    private String useDefaultStore;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        AppoTrustStore appoTrustStore = new AppoTrustStore(trustStorePath, trustStorePasswd, useDefaultStore);

        String inventoryEndPoint = inventoryService + ":" + inventoryServicePort;

        Inventory inventory = new Inventory(delegateExecution, isSslEnabled, inventoryEndPoint, appoTrustStore);
        inventory.execute();
    }
}
