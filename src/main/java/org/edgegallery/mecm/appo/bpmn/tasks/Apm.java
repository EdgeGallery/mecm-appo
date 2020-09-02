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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.service.AppoRestClientService;
import org.edgegallery.mecm.appo.utils.AppoRestClient;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.FileChecker;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Apm extends ProcessflowAbstractTask {

    public static final String FAILED_TO_UNZIP_CSAR = "failed to unzip the csar file";
    static final int TOO_MANY = 1024;
    static final int TOO_BIG = 104857600;
    private static final Logger LOGGER = LoggerFactory.getLogger(Apm.class);
    private static final String FAILED_TO_CREATE_DIR = "failed to create local directory";
    private static final String FAILED_TO_GET_PATH = "failed to get local directory path";
    private final DelegateExecution delegateExecution;
    private final String operation;
    private final String packagePath;
    private String baseUrl;
    private AppoRestClientService restClientService;

    /**
     * Constructor for APM.
     *
     * @param delegateExecution delegate execution
     * @param servicePort       apm end point
     */
    public Apm(DelegateExecution delegateExecution, String servicePort, String packagePath,
               AppoRestClientService appoRestClientService) {
        this.delegateExecution = delegateExecution;
        restClientService = appoRestClientService;
        baseUrl = servicePort;

        this.packagePath = packagePath;
        this.operation = (String) delegateExecution.getVariable("operationType");
    }

    /**
     * Creates directory to save config file.
     *
     * @param dirPath directory path to be created
     * @return directory's canonical path
     */
    private static String createDir(String dirPath) {
        File localFileDir = new File(dirPath);
        if (!localFileDir.mkdir()) {
            LOGGER.info(FAILED_TO_CREATE_DIR);
            throw new AppoException(FAILED_TO_CREATE_DIR);
        }

        try {
            return localFileDir.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.info(FAILED_TO_GET_PATH);
            throw new AppoException(FAILED_TO_GET_PATH);
        }
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
            String tenantId = (String) delegateExecution.getVariable(Constants.TENANT_ID);
            String appPkgId = (String) delegateExecution.getVariable(Constants.APP_PACKAGE_ID);

            UrlUtil urlUtil = new UrlUtil();
            urlUtil.addParams(Constants.TENANT_ID, tenantId);
            urlUtil.addParams(Constants.APP_PACKAGE_ID, appPkgId);
            String downloadUrl = urlUtil.getUrl(baseUrl + Constants.APM_DOWNLOAD_URI);

            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            downloadPackage(downloadUrl, appPkgId, appInstanceId);

            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    void downloadPackage(String url, String appPackageId, String appInstanceId) {
        LOGGER.info("Download application package {}", appPackageId);

        String accessToken = (String) delegateExecution.getVariable(Constants.ACCESS_TOKEN);

        AppoRestClient client = restClientService.getAppoRestClient();
        client.addHeader(Constants.ACCESS_TOKEN, accessToken);

        try (CloseableHttpResponse response = client.sendRequest(Constants.GET, url)) {
            if (response == null) {
                LOGGER.info("Application package download failed...");
                return;
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < Constants.HTTP_STATUS_CODE_200 || statusCode > Constants.HTTP_STATUS_CODE_299) {
                String error = getErrorInfo(response, "Download application package failed");
                setProcessflowErrorResponseAttributes(delegateExecution, error, String.valueOf(statusCode));
            } else {
                String appPackage = copyApplicationPackage(appInstanceId, appPackageId, response.getEntity());
                Boolean isValid = validateApplicationPackage(appPackage);
                if (isValid.equals(true)) {
                    setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, String.valueOf(statusCode));
                    return;
                }
                setProcessflowResponseAttributes(delegateExecution, "Invalid application package",
                        Constants.PROCESS_FLOW_ERROR);
            }
        } catch (IOException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution,
                    "Application package download failed, io exception", Constants.PROCESS_FLOW_ERROR);
        } catch (ParseException | AppoException | IllegalArgumentException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    private String copyApplicationPackage(String appInstanceId, String appPackageId, HttpEntity entity) {

        LOGGER.info("Copy application package {}", appPackageId);
        if (entity == null) {
            throw new IllegalArgumentException("Failed to copy application package " + appPackageId);
        }
        String localDirPath = createDir(packagePath + Constants.SLASH + appInstanceId);
        String appPackagePath = localDirPath + Constants.SLASH + appPackageId + Constants.APP_PKG_EXT;
        File appPackage = new File(appPackagePath);

        try (InputStream inputStream = entity.getContent();
                OutputStream outputStream = new FileOutputStream(appPackage);) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            LOGGER.info("Failed to copy application package {}", appPackageId);
            throw new AppoException("Failed to copy application package " + appPackageId);
        }
        return appPackagePath;
    }

    /**
     * Validates application package.
     *
     * @param appPackage CSAR file path
     * @return main service template content in string
     */
    public Boolean validateApplicationPackage(String appPackage) {

        try (ZipFile zipFile = new ZipFile(appPackage)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int entriesCount = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                entriesCount++;
                if (entriesCount > TOO_MANY || entry.getSize() > TOO_BIG) {
                    LOGGER.info("Too many files to unzip or file size is too big");
                    return Boolean.FALSE;
                } else {
                    FileChecker.check(new File(entry.getName()));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.error(FAILED_TO_UNZIP_CSAR);
            throw new AppoException(FAILED_TO_UNZIP_CSAR);
        }
        return Boolean.TRUE;
    }
}
