package org.edgegallery.mecm.appo.service;

import org.apache.http.impl.client.CloseableHttpClient;
import org.edgegallery.mecm.appo.apihandler.AppoSyncHandler;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstanceDeletedDto;
import org.edgegallery.mecm.appo.apihandler.dto.AppInstanceInfoDto;
import org.edgegallery.mecm.appo.exception.AppoException;
import org.edgegallery.mecm.appo.model.AppInstanceInfo;
import org.edgegallery.mecm.appo.service.impl.AppoProcessflowServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestClientHelperTest.class)
@AutoConfigureMockMvc
public class RestClientHelperTest {


    @Mock
    private AppInstanceInfoService appInstanceInfoService;

    @InjectMocks
    AppoSyncHandler handler;

    RestClientHelper restClientHelper;

    private static final String TENANT_ID = "12db0288-3c67-4042-a708-a8e4a10c6b31";


    @Test
    public void buildHttpClient() {
        restClientHelper = new RestClientHelper(true, "path", "trust");
        assertThrows(AppoException.class, () -> restClientHelper.buildHttpClient());
        restClientHelper.setTrustStorePasswd("truststore");
        restClientHelper.setSslEnabled(false);
        restClientHelper.setTrustStorePath("text");
        RestClientHelper restClientHelper2 = new RestClientHelper(restClientHelper.isSslEnabled(),
                restClientHelper.getTrustStorePath(),
                restClientHelper.getTrustStorePasswd());
        CloseableHttpClient httpClient = restClientHelper2.buildHttpClient();
        assertNotNull(httpClient);

    }

    @Test(expected = InvocationTargetException.class)
    public void testProcessflowErrorResponse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AppoProcessflowServiceImpl appoProcessflowService = new AppoProcessflowServiceImpl();
        Object[] obj1 = {"processInstanceId", "OK"};
        Method method1 = AppoProcessflowServiceImpl.class.getDeclaredMethod("processflowErrorResponse", String.class, String.class);
        method1.setAccessible(true);
        method1.invoke(appoProcessflowService, obj1);
    }


    @Test(expected = InvocationTargetException.class)
    public void testProcessflowException() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AppoProcessflowServiceImpl appoProcessflowService = new AppoProcessflowServiceImpl();
        Object[] obj1 = {"processInstanceId", "OK"};
        Method method1 = AppoProcessflowServiceImpl.class.getDeclaredMethod("processflowException", String.class, String.class);
        method1.setAccessible(true);
        method1.invoke(appoProcessflowService, obj1);
    }

    @Test
    public void testUpdateSyncAppInstanceRecords() throws Exception {
        AppInstanceInfoDto appInstanceInfoDto = new AppInstanceInfoDto();
        appInstanceInfoDto.setAppInstanceId("instance_id");

        AppInstanceInfo appInstanceInfo = new AppInstanceInfo();
        Mockito.when(appInstanceInfoService.getAppInstanceInfo(TENANT_ID, appInstanceInfoDto.getAppInstanceId())).thenReturn(appInstanceInfo);

        Object[] obj1 = {TENANT_ID, "1.1.1.1", appInstanceInfoDto};
        Method method1 = AppoSyncHandler.class.getDeclaredMethod("updateSyncAppInstanceRecords", String.class, String.class, AppInstanceInfoDto.class);
        method1.setAccessible(true);
        method1.invoke(handler, obj1);
    }

    @Test
    public void testDeleteSyncAppInstanceRecords() throws Exception {
        AppInstanceDeletedDto appInstanceDeletedDto = new AppInstanceDeletedDto();
        appInstanceDeletedDto.setAppInstanceId("instance_id");

        Object[] obj1 = {TENANT_ID, appInstanceDeletedDto};
        Method method1 = AppoSyncHandler.class.getDeclaredMethod("deleteSyncAppInstanceRecords", String.class, AppInstanceDeletedDto.class);
        method1.setAccessible(true);
        method1.invoke(handler, obj1);
    }
}
