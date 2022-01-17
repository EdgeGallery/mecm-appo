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

package org.edgegallery.mecm.appo.service;

import org.edgegallery.mecm.appo.apihandler.dto.SyncBaseDto;
import org.edgegallery.mecm.appo.utils.AppoV2Response;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Sync service to synchronize records with MEPM.
 */
public interface RestService {

    /**
     * Synchronizes updated or inserted records.
     *
     * @param url           url of MEPM component
     * @param responseClass class to which response needs to be mapped
     * @param token         access token
     * @param <T>           type of body
     * @return response entity with body of type T
     */
    <T extends SyncBaseDto> ResponseEntity<T> syncRecords(String url, Class<T> responseClass, String token);

    /**
     * Send requests to desired end point.
     *
     * @param uri uri of end point
     * @param method http method
     * @param token access token
     * @param data body
     * @return response entity
     */
    ResponseEntity<String> sendRequest(String uri, HttpMethod method, String token, String data);

    ResponseEntity<AppoV2Response> sendRequestResourceManager(String uri, HttpMethod method, String token,
                                                              String data);
}