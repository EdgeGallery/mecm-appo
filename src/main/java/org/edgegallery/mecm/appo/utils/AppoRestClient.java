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

package org.edgegallery.mecm.appo.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppoRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppoRestClient.class);

    private Map<String, String> headerMap;
    private HttpEntity data = null;
    private AppoBuildClient clientBuilder;
    private Boolean isSslEnable;

    /**
     * Creates rest client instance.
     */
    public AppoRestClient() {
        this.headerMap = new HashMap<>();

        addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        addHeader("Accept", ContentType.APPLICATION_JSON.toString());
        this.clientBuilder = new AppoBuildClient(false, null, null);
    }

    /**
     * Creates rest client instance.
     */
    public AppoRestClient(Boolean isSslEnabled, String trustStorePath, String trustStorePasswd) {
        isSslEnable = isSslEnabled;
        headerMap = new HashMap<>();
        addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        addHeader("Accept", ContentType.APPLICATION_JSON.toString());
        clientBuilder = new AppoBuildClient(isSslEnabled, trustStorePath, trustStorePasswd);
    }

    /**
     * Retrieves error information from response.
     *
     * @param response http response
     * @param error    error string
     * @return error info
     * @throws IOException    io exception
     * @throws ParseException parse exception
     */
    public String getErrorInfo(CloseableHttpResponse response, String error)
            throws IOException, ParseException {
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonResponse = (JSONObject) new JSONParser().parse(responseStr);
        if (jsonResponse.get("error") != null) {
            return jsonResponse.get("error").toString();
        }
        return error;
    }

    /**
     * Adds header parameter to the request.
     *
     * @param name  parameter name
     * @param value parameter value
     */
    public void addHeader(String name, String value) {
        LOGGER.info("Add header...");
        try {
            if (!name.isEmpty() && !value.isEmpty()) {
                headerMap.put(name, value);
            } else {
                LOGGER.warn("Cannot add, invalid input name: {} , value: {}", name, value);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Adds authentication header to the request.
     *
     * @param name  parameter name
     * @param value parameter value
     */
    public void addAuthHeader(String name, String value) {
        LOGGER.info("Add auth header...");
        if (name.isEmpty() && value.isEmpty()) {
            headerMap.put(name, value);
        } else {
            LOGGER.warn("Cannot add, invalid input name: {} , value: {}", name, value);
        }
    }

    /**
     * Gets http entity.
     *
     * @param filePath file to send
     */
    public void addFileEntity(String filePath) {
        LOGGER.info("Add file entity...");
        try {
            File file = new File(filePath);
            data = MultipartEntityBuilder.create()
                    .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName()).build();
        } catch (Exception e) {
            LOGGER.error("Failed to encode entity {}", e.getMessage());
        }
    }

    /**
     * Gets http entity.
     *
     * @param requestBody body to send
     */
    public void addEntity(String requestBody) {
        LOGGER.info("Add entity...");
        try {
            data = new StringEntity(requestBody);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to encode entity {}", e.getMessage());
        }
    }

    /**
     * Sends http request to remote entity.
     *
     * @param method request method
     * @param uri    uri
     * @return http response on success, error on failure
     */
    public CloseableHttpResponse sendRequest(String method, String uri) {
        LOGGER.info("Send http {} request", method);
        CloseableHttpResponse httpclient = null;
        try {
            URL url;
            if (isSslEnable.equals(true)) {
                url = new URL("https://" + uri);
            } else {
                url = new URL("http://" + uri);
            }
            LOGGER.info("Send http request: method: {},  url: {}", method, url);
            switch (method) {
                case Constants.GET:
                    httpclient = doGet(url);
                    break;
                case Constants.POST:
                    httpclient = doPost(url);
                    break;
                case Constants.DELETE:
                    httpclient = doDelete(url);
                    break;
                default:
                    LOGGER.info("Method not allowed {}", method);
                    throw new AppoException("Method not allowed");
            }
        } catch (IOException | AppoException e) {
            throw new AppoException("Failed to send request ");
        }
        return httpclient;
    }

    private CloseableHttpResponse doGet(URL url) throws IOException {
        LOGGER.info("doGet...");

        CloseableHttpClient client = null;
        HttpGet httpGet = new HttpGet(url.toString());
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            httpGet.addHeader(entry.getKey(), entry.getValue());
        }
        client = clientBuilder.buildClient();
        return client.execute(httpGet);
    }

    private CloseableHttpResponse doPost(URL url) throws IOException {
        LOGGER.info("doPost...");
        CloseableHttpClient client = null;

        HttpPost httpPost = new HttpPost(url.toString());
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            httpPost.addHeader(entry.getKey(), entry.getValue());
        }
        httpPost.setEntity(data);
        client = clientBuilder.buildClient();

        return client.execute(httpPost);
    }

    private CloseableHttpResponse doDelete(URL url) throws IOException {
        LOGGER.info("doDelete...");
        CloseableHttpClient client = null;

        HttpDelete httpDelete = new HttpDelete(url.toString());
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            httpDelete.addHeader(entry.getKey(), entry.getValue());
        }

        client = clientBuilder.buildClient();
        return client.execute(httpDelete);
    }
}