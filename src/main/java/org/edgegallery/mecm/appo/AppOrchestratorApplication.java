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

package org.edgegallery.mecm.appo;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executor;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.edgegallery.mecm.appo.service.RestClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * Edge application orchestrator.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableAsync
@EnableProcessApplication
@EnableServiceComb
public class AppOrchestratorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppOrchestratorApplication.class);

    @Value("${appo.async.corepool-size}")
    private int corePoolSize;

    @Value("${appo.async.maxpool-size}")
    private int maxPoolSize;

    @Value("${appo.async.queue-capacity}")
    private int queueCapacity;

    /**
     * Edge application orchestrator entry function.
     *
     * @param args arguments
     */
    public static void main(String[] args) {

        LOGGER.info("Edge application orchestrator starting----");

        // do not check host name
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                LOGGER.info("checks client trusted");
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                LOGGER.info("checks server trusted");
            }
        }
        };
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(NoopHostnameVerifier.INSTANCE);

            SpringApplication.run(AppOrchestratorApplication.class, args);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            LOGGER.info("SSL context init error... exiting system {}", e.getMessage());
        }
    }


    /**
     * Asychronous configurations.
     *
     * @return thread pool task executor
     */
    @Bean
    @Primary
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("appo-Camunda-");
        executor.initialize();
        return executor;
    }

}
