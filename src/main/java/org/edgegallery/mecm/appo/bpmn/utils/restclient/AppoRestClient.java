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

package org.edgegallery.mecm.appo.bpmn.utils.restclient;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppoRestClient {

    private static final Logger logger = LoggerFactory.getLogger(AppoRestClient.class);

    public static final int CONNECTION_TIMEOUT = 10_000;
    public static final int SOCKET_TIMEOUT = 30_000;

    public static final int MAX_RETRY = 3;
    public static final int WAIT_PERIOD = 10;

    private Map<String, String> headerMap;
    private HttpEntity data = null;

    public AppoRestClient() {
        this.headerMap = new HashMap<>();
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
        return jsonResponse.get("error") != null ? jsonResponse.get("error").toString() : error;
    }

    /**
     * Adds header parameter to the request.
     *
     * @param name  parameter name
     * @param value parameter value
     */
    public void addHeader(String name, String value) {
        try {
            if (!name.isEmpty() && !value.isEmpty()) {
                headerMap.put(name, value);
            } else {
                logger.warn("Cannot add, invalid input name: {} , value: {}", name, value);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Adds authentication header to the request.
     *
     * @param name  parameter name
     * @param value parameter value
     */
    public void addAuthHeader(String name, String value) {
        if (name.isEmpty() && value.isEmpty()) {
            headerMap.put(name, value);
        } else {
            logger.warn("Cannot add, invalid input name: {} , value: {}", name, value);
        }
    }

    /**
     * Gets http entity.
     *
     * @param filePath file to send
     */
    public HttpEntity addFileEntity(String filePath) {
        HttpEntity entity = null;
        try {
            File file = new File(filePath);
            // build multipart upload request
            entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName()).build();
            return entity;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return data;
    }

    /**
     * Gets http entity.
     *
     * @param requestBody body to send
     */
    public HttpEntity addEntity(String requestBody) {
        HttpEntity entity = null;
        try {
            entity = new StringEntity(requestBody);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return entity;
    }

    /**
     * Send post request to server.
     *
     * @param url    post url
     * @param entity request entity
     * @return http response
     * @throws AppoException on exceptionn
     */
    public CloseableHttpResponse doPost(String url, HttpEntity entity) {
        logger.info("Send POST request, url {}", url);
        try {
            URL postUrl = new URL(url);
            HttpPost post = new HttpPost(postUrl.toString());
            post.setEntity(entity);

            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }

            return sendRequest(post);
        } catch (IOException e) {
            throw new AppoException("Failed to send instantiate request " + e.getMessage());
        }
    }

    /**
     * Send get request to server.
     *
     * @param url get url
     * @return http response
     * @throws AppoException on exceptionn
     */
    public CloseableHttpResponse doGet(String url) {
        logger.info("Send GET request, url {}", url);
        try {
            URL getUrl = new URL(url);
            HttpGet httpGet = new HttpGet(getUrl.toString());

            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
            return sendRequest(httpGet);
        } catch (IOException e) {
            throw new AppoException("Failed to send get request " + e.getMessage());
        }
    }

    /**
     * Send delete request to server.
     *
     * @param url delete url
     * @return http response
     * @throws AppoException on exceptionn
     */
    public CloseableHttpResponse doDelete(String url) {
        logger.info("Send DELETE request, url {}", url);
        try {
            URL deleteUrl = new URL(url);
            HttpDelete httpDelete = new HttpDelete(deleteUrl.toString());

            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpDelete.addHeader(entry.getKey(), entry.getValue());
            }
            return sendRequest(httpDelete);
        } catch (IOException e) {
            throw new AppoException("Delete operation failed {} " + e.getMessage());
        }
    }

    private CloseableHttpResponse sendRequest(HttpRequestBase httpRequest) throws IOException {

        logger.info("Sending request : {}", httpRequest.getURI());

        CloseableHttpClient httpclient = buildHttpClient(httpRequest);

        return httpclient.execute(httpRequest);
    }

    private static HttpRequestRetryHandler retryMechanism(int maxRetry) {
        return (exception, retries, ctx) -> {

            if (retries >= maxRetry) {
                return false;
            }
            if (exception instanceof InterruptedIOException
                    || exception instanceof UnknownHostException
                    || exception instanceof SSLException) {
                return false;
            }

            if (exception instanceof HttpHostConnectException) {
                return true;
            }
            HttpClientContext clientCtx = HttpClientContext.adapt(ctx);
            HttpRequest request = clientCtx.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);

            return idempotent ? Boolean.TRUE : Boolean.FALSE;
        };
    }

    /**
     * Retrieves HHTP client.
     *
     * @param httpRequest http request
     * @return http client on success
     */
    public static CloseableHttpClient buildHttpClient(HttpRequestBase httpRequest) {
        CloseableHttpClient httpClient = null;
        if (httpRequest.getURI().toString().startsWith("https")) {
            SSLContext sslcxt = null;

            try {
                sslcxt = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                throw new AppoException("SSL context failed....");
            }

            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslcxt, (s, sslSession) -> false);

            final HostnameVerifier allHostsValid = (hostname, session) -> false;

            httpClient = HttpClients.custom().setRetryHandler(retryMechanism(MAX_RETRY))
                                             .setServiceUnavailableRetryStrategy(
                                                     new DefaultServiceUnavailableRetryStrategy(MAX_RETRY, WAIT_PERIOD))
                                             .setSSLSocketFactory(sslFactory)
                                             .setSSLHostnameVerifier(allHostsValid).build();
        } else {
            httpClient = HttpClients.custom().setRetryHandler(retryMechanism(MAX_RETRY))
                                             .setServiceUnavailableRetryStrategy(
                                                     new DefaultServiceUnavailableRetryStrategy(MAX_RETRY, WAIT_PERIOD))
                                             .build();
        }
        return httpClient;
    }
}
