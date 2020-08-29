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
import org.edgegallery.mecm.appo.bpmn.utils.UrlUtility;
import org.edgegallery.mecm.appo.bpmn.utils.restclient.AppoRestClient;
import org.edgegallery.mecm.appo.common.Constants;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Apm extends ProcessflowAbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Apm.class);

    private final DelegateExecution delegateExecution;
    private final String operation;
    private final String apmDownloadUrl;
    private final String packagePath;

    /**
     * Constructor for APM.
     *
     * @param delegateExecution delegate execution
     * @param isSslEnabled      ssl enabled
     * @param endPoint          apm end point
     */
    public Apm(DelegateExecution delegateExecution, String isSslEnabled, String endPoint, String packagePath) {
        this.delegateExecution = delegateExecution;
        String protocol = getProtocol(isSslEnabled);
        String baseUrl = protocol + endPoint;

        this.apmDownloadUrl = baseUrl + Constants.APM_DOWNLOAD_URI;
        this.packagePath = packagePath;
        this.operation = (String) delegateExecution.getVariable("operationType");
    }

    /**
     * Retrieves specified inventory.
     */
    public void execute() {
        if (operation.equals("download")) {
            download(delegateExecution);
        } else {
            LOGGER.info("Invalid APM action...{}", operation);
            setProcessflowExceptionResponseAttributes(delegateExecution, "Invalid APM action",
                    Constants.PROCESS_FLOW_ERROR);
        }
    }

    private void download(DelegateExecution delegateExecution) {

        LOGGER.info("Download package from APM");
        try {

            String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);
            AppoRestClient client = new AppoRestClient();
            client.addHeader(Constants.ACCESS_TOKEN, accessToken);

            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            String appPkgId = (String) delegateExecution.getVariable(Constants.APP_PACKAGE_ID);

            UrlUtility urlUtil = new UrlUtility();
            urlUtil.addParams(Constants.TENANT_ID, tenantId);
            urlUtil.addParams(Constants.APP_PACKAGE_ID, appPkgId);
            String url = urlUtil.getUrl(apmDownloadUrl);

            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            downloadPackage(url, appPkgId, appInstanceId);

            setProcessflowResponseAttributes(delegateExecution, "OK", Constants.PROCESS_FLOW_SUCCESS);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    void downloadPackage(String url, String appPackageId, String appInstanceId) {

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
