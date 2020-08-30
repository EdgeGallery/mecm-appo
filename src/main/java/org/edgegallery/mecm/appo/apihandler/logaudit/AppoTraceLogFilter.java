/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.mecm.appo.apihandler.logaudit;

import com.google.gson.Gson;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

class AppoTraceLogFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppoTraceLogFilter.class);
    public static final int OFFSET = 10;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - OFFSET;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        String accessId = UUID.randomUUID().toString();
        try {
            logForRequest(accessId, request);
            filterChain.doFilter(request, response);
        } finally {
            logForResponse(accessId, response);
            updateResponse(response);
        }
    }

    private void logForRequest(String accessId, HttpServletRequest request) {
        HttpRequestTraceLog requestTraceLog = new HttpRequestTraceLog();
        requestTraceLog.setAccessId(accessId);
        requestTraceLog.setTime(LocalDateTime.now().toString());
        requestTraceLog.setPath(request.getRequestURI());
        requestTraceLog.setMethod(request.getMethod());
        requestTraceLog.setRemoteAddr(request.getRemoteAddr());
        String dump = new Gson().toJson(requestTraceLog);
        LOGGER.info("Http request trace log: {}", dump);
    }

    private void logForResponse(String accessId, HttpServletResponse response) {
        HttpResponseTraceLog responseTraceLog = new HttpResponseTraceLog();
        responseTraceLog.setAccessId(accessId);
        responseTraceLog.setStatus(response.getStatus());
        responseTraceLog.setTime(LocalDateTime.now().toString());
        String dump = new Gson().toJson(responseTraceLog);
        LOGGER.info("Http response trace log: {}", dump);
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils
                .getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            responseWrapper.copyBodyToResponse();
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    private static class HttpRequestTraceLog {

        private String accessId;
        private String path;
        private String method;
        private String time;
        private String remoteAddr;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    private static class HttpResponseTraceLog {

        private String accessId;
        private Integer status;
        private String time;
    }
}
