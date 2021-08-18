/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.mecm.appo.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.edgegallery.mecm.appo.apihandler.dto.SyncBaseDto;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.service.RestService;
import org.edgegallery.mecm.appo.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of rest service.
 */
@Service("SyncServiceImpl")
public class RestServiceImpl implements RestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceImpl.class);
    private static final String ACCESS_TOKEN = "access_token";
    private static final String HTTPS_PROTO = "https://";
    private static final String HTTP_PROTO = "http://";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${server.ssl.enabled:false}")
    private String isSslEnabled;

    @Override
    public <T extends SyncBaseDto> ResponseEntity<T> syncRecords(String uri, Class<T> responseClass, String token) {
        String protocol = HTTP_PROTO;
        if ("true".equals(isSslEnabled)) {
            protocol = HTTPS_PROTO;
        }
        String url = protocol + uri;

        // Preparing HTTP header
        HttpHeaders httpHeaders = getHttpHeader(token);

        // Creating HTTP entity with headers
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity responseEntity;
        // Sending request
        try {
            LOGGER.info("GET: {}", url);
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseClass);
        } catch (RestClientException e) {
            throw new AppoException("Failure during sync MEPM with error message: "
                    + e.getLocalizedMessage());
        }
        LOGGER.info("Sync status code {}, value {} ", responseEntity.getStatusCodeValue(), responseEntity.getBody());

        HttpStatus statusCode = responseEntity.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new AppoException("Failure while sync file to MEPM with not successful status code: "
                    + statusCode);
        }
        return responseEntity;
    }

    @Override
    public ResponseEntity<String> sendRequest(String uri, HttpMethod method, String token, String data) {
        String protocol = HTTP_PROTO;
        if ("true".equals(isSslEnabled)) {
            protocol = HTTPS_PROTO;
        }
        String url = protocol + uri;

        // Preparing HTTP header
        HttpHeaders httpHeaders = getHttpHeader(token);

        // Creating HTTP entity with headers
        HttpEntity<String> httpEntity = null;
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            httpEntity = new HttpEntity<>(data, httpHeaders);
        } else if (method == HttpMethod.DELETE || method == HttpMethod.GET) {
            httpEntity = new HttpEntity<>(httpHeaders);
        }

        ResponseEntity<String> responseEntity;
        // Sending request
        try {
            LOGGER.info("{}: {}", method, url);

            responseEntity = restTemplate.exchange(url, method, httpEntity, String.class);
        } catch (RestClientException e) {
            throw new AppoException("Failure while sending request with error message: "
                    + e.getLocalizedMessage());
        }
        LOGGER.info("Send request status code {}, value {} ", responseEntity.getStatusCodeValue(),
                responseEntity.getBody());

        HttpStatus statusCode = responseEntity.getStatusCode();
        if (Constants.PROCESS_RECORD_NOT_FOUND.equals(statusCode.toString())) {
            throw new NoSuchElementException("Record not found status code: " + statusCode);
        }

        if (!statusCode.is2xxSuccessful()) {
            throw new AppoException("Failure while sending request status code: " + statusCode);
        }
        return responseEntity;
    }

    private HttpHeaders getHttpHeader(String token) {
        List<MediaType> list = new LinkedList<>();
        list.add(MediaType.ALL);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(list);

        httpHeaders.set(ACCESS_TOKEN, token);
        return httpHeaders;
    }
}