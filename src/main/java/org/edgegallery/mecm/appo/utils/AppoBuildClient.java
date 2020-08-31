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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppoBuildClient {

    public static final int MAX_RETRY = 3;
    public static final int WAIT_PERIOD = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppoBuildClient.class);

    private AppoTrustStore appoTrustStore = null;

    public AppoBuildClient(AppoTrustStore trustStore) {
        appoTrustStore = trustStore;
    }

    private Boolean isRetryAllowed(IOException exception, int retries, int maxRetry) {
        if (retries >= maxRetry) {
            return false;
        }
        return !(exception instanceof InterruptedIOException) && !(exception instanceof UnknownHostException)
                && !(exception instanceof SSLException) && !(exception instanceof HttpHostConnectException);
    }

    private HttpRequestRetryHandler retryMechanism(int maxRetry) {
        return (IOException exception, int retries, HttpContext ctx) -> {

            if (Boolean.FALSE.equals(isRetryAllowed(exception, retries, maxRetry))) {
                return false;
            }

            HttpClientContext clientCtx = HttpClientContext.adapt(ctx);
            HttpRequest request = clientCtx.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        };
    }

    private KeyStore getKeyStore(String keyStorePath, String password) {
        KeyStore ks = null;
        try (FileInputStream is = new FileInputStream(keyStorePath)) {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, password.toCharArray());
        } catch (CertificateException | KeyStoreException
                | NoSuchAlgorithmException | IOException e) {
            throw new AppoException("keystore exception");
        }
        return ks;
    }


    /**
     * Retrieves HTTP client.
     *
     * @param httpRequest http request
     * @return http client on success
     */
    public CloseableHttpClient buildHttpClient(HttpRequestBase httpRequest) {
        LOGGER.info("Build Http client...");
        CloseableHttpClient httpClient = null;
        KeyStore ks = null;
        try {
            if (httpRequest.getURI().toString().startsWith("https")) {
                SSLContext sslcxt = null;
                if (appoTrustStore != null && appoTrustStore.getUseDefaultStore().equals("false")) {
                    ks = getKeyStore(appoTrustStore.getTrustStorePath(), appoTrustStore.getTrustStorePasswd());
                }

                sslcxt = SSLContexts.custom()
                        .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
                        .setProtocol("TLSv1.2")
                        .build();
                SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslcxt,
                    (s, sslSession) -> false);

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
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            LOGGER.info("Failed to build client...{}", e.getMessage());
            throw new AppoException(e.getMessage());
        }
        return httpClient;
    }
}