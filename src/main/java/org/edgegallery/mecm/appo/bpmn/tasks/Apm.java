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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.utils.AppoRestClient;
import org.edgegallery.mecm.appo.utils.AppoTrustStore;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Apm extends ProcessflowAbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Apm.class);

    private final DelegateExecution delegateExecution;
    private final String operation;
    private final String packagePath;
    private String baseUrl;
    private AppoTrustStore appoTrustStore;


    /**
     * Constructor for APM.
     *
     * @param delegateExecution delegate execution
     * @param isSslEnabled      ssl enabled
     * @param endPoint          apm end point
     */
    public Apm(DelegateExecution delegateExecution, String isSslEnabled, String endPoint,
               String packagePath, AppoTrustStore trustStore) {
        this.delegateExecution = delegateExecution;
        appoTrustStore = trustStore;
        baseUrl = getProtocol(isSslEnabled) + endPoint;

        this.packagePath = packagePath;
        this.operation = (String) delegateExecution.getVariable("operationType");
    }

    /**
     * Retrieves specified inventory.
     */
    public void execute() {
        if (operation.equals("download")) {
            download(delegateExecution, baseUrl + Constants.APM_DOWNLOAD_URI);
        } else {
            LOGGER.info("Invalid APM action...{}", operation);
            setProcessflowExceptionResponseAttributes(delegateExecution, "Invalid APM action",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void download(DelegateExecution delegateExecution, String url) {

        LOGGER.info("Download package from APM");
        try {

            String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
            AppoRestClient client = new AppoRestClient();
            client.addHeader(Constants.ACCESS_TOKEN, accessToken);

            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            String appPkgId = (String) delegateExecution.getVariable(Constants.APP_PACKAGE_ID);

            UrlUtil urlUtil = new UrlUtil();
            urlUtil.addParams(Constants.TENANT_ID, tenantId);
            urlUtil.addParams(Constants.APP_PACKAGE_ID, appPkgId);
            String downloadUrl = urlUtil.getUrl(url);

            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            downloadPackage(downloadUrl, appPkgId, appInstanceId);

            setProcessflowResponseAttributes(delegateExecution, "OK", Constants.PROCESS_FLOW_SUCCESS);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    void downloadPackage(String url, String appPackageId, String appInstanceId) {
        LOGGER.info(" {}", appoTrustStore);
        try (InputStream inputStream = new URL(url).openStream();
                FileOutputStream fileOs = new FileOutputStream(packagePath + appInstanceId + "/" + appPackageId)) {
            IOUtils.copy(inputStream, fileOs);
        } catch (MalformedURLException | FileNotFoundException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            throw new AppoException(e.getMessage());
        } catch (IOException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
            LOGGER.debug("Failed to download application package from APM");
            throw new AppoException(e.getMessage());
        }
    }
}
