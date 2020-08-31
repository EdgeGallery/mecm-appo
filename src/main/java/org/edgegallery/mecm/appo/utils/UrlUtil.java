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

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtil.class);

    private Map<String, String> pathParams;

    public UrlUtil() {
        this.pathParams = new HashMap<>();
    }

    /**
     * Adds parameter required to replace in URL.
     *
     * @param name  parameter name
     * @param value parameter value
     */
    public void addParams(String name, String value) {
        if (name == null || value == null) {
            LOGGER.warn("Cannot add, invalid input");
            throw new IllegalArgumentException("Invalid input");
        }
        if (!name.isEmpty() && !value.isEmpty()) {
            pathParams.put(name, value);
        } else {
            LOGGER.warn("Cannot add, invalid input name: {} , value: {}", name, value);
            throw new IllegalArgumentException("Invalid input");
        }
    }

    /**
     * Replaces path variables in URL.
     *
     * @param urlString  url string
     * @param parameters parameters to replace
     * @return url string with replaced parameters
     */
    public String replacePathParamsInUrl(String urlString, Map<String, String> parameters) {
        UriBuilder builder = UriBuilder.fromPath(urlString);
        return builder.buildFromMap(parameters).toString();
    }

    /**
     * Retrieves URL string with actual path parameters.
     *
     * @param url url string
     * @return url on success otherwise exception
     */
    public String getUrl(String url) {
        return replacePathParamsInUrl(url, pathParams);
    }
}
