/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.mecm.appo.model;

import org.edgegallery.mecm.appo.apihandler.filter.AccessTokenFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccessTokenFilterTest.class)
public class AccessTokenFilterTest {

    public static final String HEALTH_URI = "/apm/v1/health";
    public static final String APP_URI = "/apm/v1/app";

  //  @Autowired
    AccessTokenFilter filter;

    HttpServletRequest mockReq;
    HttpServletResponse mockResp;
    FilterChain mockFilterChain;
    FilterConfig mockFilterConfig;
    OAuth2AccessToken oAuth2AccessToken;

    @Before
    public void onStartup() {
        filter = new AccessTokenFilter();
        mockFilterChain = mock(FilterChain.class);
        mockFilterConfig = mock(FilterConfig.class);
        oAuth2AccessToken = mock(OAuth2AccessToken.class);
    }

    @Test
    public void testDoFilterHealthUri() throws ServletException, IOException {
        mockReq = mock(HttpServletRequest.class);
        mockResp = mock(HttpServletResponse.class);

        Mockito.when(mockReq.getRequestURI()).thenReturn(HEALTH_URI);
        BufferedReader br = new BufferedReader(new StringReader("test"));

        filter.doFilter(mockReq, mockResp, mockFilterChain);
    }

    @Test
    public void testDoFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ServletException, IOException {
        mockReq = mock(HttpServletRequest.class);
        mockResp = mock(HttpServletResponse.class);

        Mockito.when(mockReq.getRequestURI()).thenReturn(APP_URI);
        filter.doFilter(mockReq, mockResp, mockFilterChain);

        Object[] obj1 = {"ok/success/yes"};
        Method method1 = AccessTokenFilter.class.getDeclaredMethod("getTenantId", String.class);
        method1.setAccessible(true);
        method1.invoke(filter, obj1);
    }

}



