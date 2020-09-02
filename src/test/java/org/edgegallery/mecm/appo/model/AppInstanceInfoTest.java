package org.edgegallery.mecm.appo.model;

import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class AppInstanceInfoTest {

    @InjectMocks
    AppInstanceInfo appInstanceInfo = new AppInstanceInfo();

    @Before
    public void setUp() {
        appInstanceInfo.setAppDescriptor(AppoConstantsTest.App_Descriptor);
        appInstanceInfo.setAppdId(AppoConstantsTest.APP_ID);
        appInstanceInfo.setAppInstanceId(AppoConstantsTest.App_Instance_Id);
        appInstanceInfo.setAppName(AppoConstantsTest.App_Name);
        appInstanceInfo.setApplcmHost(AppoConstantsTest.Applcm_LCM_HOST);
        appInstanceInfo.setAppPackageId(AppoConstantsTest.App_Package_Id);
        appInstanceInfo.setMecHost(AppoConstantsTest.Mec_Host);
        appInstanceInfo.setTenant(AppoConstantsTest.Tenant);
        appInstanceInfo.setOperationalStatus("operationalStatus");
        appInstanceInfo.setOperationInfo("operationalInfo");
    }

    @Test
    public void testAppInstanceInfoDto() {
        Assert.assertEquals(AppoConstantsTest.App_Descriptor, appInstanceInfo.getAppDescriptor());
        Assert.assertEquals(AppoConstantsTest.APP_ID, appInstanceInfo.getAppdId());
        Assert.assertEquals(AppoConstantsTest.App_Instance_Id, appInstanceInfo.getAppInstanceId());
        Assert.assertEquals(AppoConstantsTest.App_Name, appInstanceInfo.getAppName());
        Assert.assertEquals(AppoConstantsTest.Applcm_LCM_HOST, appInstanceInfo.getApplcmHost());
        Assert.assertEquals(AppoConstantsTest.App_Package_Id, appInstanceInfo.getAppPackageId());
        Assert.assertEquals(AppoConstantsTest.Mec_Host, appInstanceInfo.getMecHost());
        Assert.assertEquals(AppoConstantsTest.Tenant, appInstanceInfo.getTenant());
        Assert.assertEquals(AppoConstantsTest.Operational_Status, appInstanceInfo.getOperationalStatus());
        Assert.assertEquals(AppoConstantsTest.Operational_Info, appInstanceInfo.getOperationInfo());
    }
}
