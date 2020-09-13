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
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.utils.Constants;
import org.edgegallery.mecm.appo.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class Apm extends ProcessflowAbstractTask {

    public static final String FAILED_TO_UNZIP_CSAR = "failed to unzip the csar file";
    static final int TOO_MANY = 1024;
    static final int TOO_BIG = 104857600;
    private static final Logger LOGGER = LoggerFactory.getLogger(Apm.class);
    private static final String FAILED_TO_CREATE_DIR = "failed to create local directory";
    private static final String FAILED_TO_GET_PATH = "failed to get local directory path";
    private final DelegateExecution execution;
    private final String operation;
    private String baseUrl;
    private String appPkgBasePath;
    private RestTemplate restTemplate;
    private String protocol = "https://";

    /**
     * Constructor for APM.
     *
     * @param delegateExecution  delegate execution
     * @param servicePort        apm end point
     * @param restClientTemplate rest client template
     */
    public Apm(DelegateExecution delegateExecution, boolean isSslEnabled, String appPkgsBasePath, String servicePort,
               RestTemplate restClientTemplate) {
        execution = delegateExecution;
        if (!isSslEnabled) {
            protocol = "http://";
        }
        restTemplate = restClientTemplate;
        baseUrl = servicePort;
        appPkgBasePath = appPkgsBasePath;
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
            download(execution);
        } else {
            LOGGER.info("Invalid APM action...{}", operation);
            setProcessflowExceptionResponseAttributes(execution, "Invalid APM action",
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
            String downloadUrl = protocol + urlUtil.getUrl(baseUrl + Constants.APM_DOWNLOAD_URI);

            String appInstanceId = (String) delegateExecution.getVariable(Constants.APP_INSTANCE_ID);

            downloadPackage(downloadUrl, appPkgId, appInstanceId);

            setProcessflowResponseAttributes(delegateExecution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);

        } catch (AppoException e) {
            setProcessflowExceptionResponseAttributes(delegateExecution, e.getMessage(), Constants.PROCESS_FLOW_ERROR);
        }
    }

    void downloadPackage(String url, String appPackageId, String appInstanceId) {
        LOGGER.info("Download application package {}", appPackageId);

        String accessToken = (String) execution.getVariable(Constants.ACCESS_TOKEN);

        ResponseEntity<Resource> response;
        Resource responseBody;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("access_token", accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOGGER.info("Download application package from APM: {}", url);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);

            responseBody = response.getBody();
            if (!HttpStatus.OK.equals(response.getStatusCode()) || responseBody == null) {
                LOGGER.error(Constants.CSAR_DOWNLOAD_FAILED, appPackageId);
                throw new AppoException("Application package download failed");
            }
            InputStream ipStream = responseBody.getInputStream();

            String appPackage = copyApplicationPackage(appInstanceId, appPackageId, ipStream);
            Boolean isValid = validateApplicationPackage(appPackage);
            if (isValid.equals(true)) {
                setProcessflowResponseAttributes(execution, Constants.SUCCESS, Constants.PROCESS_FLOW_SUCCESS);
            }
        } catch (ResourceAccessException ex) {
            LOGGER.error(Constants.FAILED_TO_CONNECT_APM);
            throw new AppoException(Constants.FAILED_TO_CONNECT_APM);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            LOGGER.error(Constants.APM_RETURN_FAILURE, ex.getResponseBodyAsString());
            throw new AppoException("Application package download failed: " + ex.getResponseBodyAsString());
        } catch (AppoException e) {
            LOGGER.error("Downloaded package is not valid");
            throw new AppoException("Downloaded package is not valid");
        } catch (IOException e) {
            LOGGER.error(Constants.GET_INPUTSTREAM_FAILED, appPackageId);
            throw new AppoException("failed to get input stream from app store response for package " + appPackageId);
        }
    }

    private String copyApplicationPackage(String appInstanceId, String appPackageId,
                                          InputStream resourceStream) {

        LOGGER.info("Copy application package {}", appPackageId);
        if (resourceStream == null) {
            LOGGER.info(Constants.FAILED_TO_READ_INPUTSTREAM, appPackageId);
            throw new AppoException("Failed to read input stream from app store for package id" + appPackageId);
        }

        String localDirPath = createDir(appPkgBasePath + appInstanceId);
        String appPackagePath = localDirPath + Constants.SLASH + appPackageId + Constants.APP_PKG_EXT;
        File appPackage = new File(appPackagePath);

        try {
            FileUtils.copyInputStreamToFile(resourceStream, appPackage);
            LOGGER.info("app package {} downloaded from APM successfully", appPackageId);
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
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.error(FAILED_TO_UNZIP_CSAR);
            throw new AppoException(FAILED_TO_UNZIP_CSAR);
        }
        return Boolean.TRUE;
    }
}
