package org.edgegallery.mecm.appo.model;

import org.edgegallery.mecm.appo.service.AppoProcessFlowResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AppoModelTest {
    @InjectMocks
    AppPackageMf appPackageMf = new AppPackageMf();
    DstInterface dstInterface = new DstInterface();
    TunnelInfo tunnelInfo = new TunnelInfo();
    AppInstanceDependency dependency = new AppInstanceDependency();
    AppInstantiateReq instantiateReq = new AppInstantiateReq();
    AppRule appRule= new AppRule();
    AppRuleTask appRuleTask= new AppRuleTask();
    AppServiceRequired serviceRequired = new AppServiceRequired();
    AppoProcessFlowResponse response = new AppoProcessFlowResponse();

    @Before
    public void setUp() {
        appPackageMf.setAppPackageDescription("appPkgDes");
        appPackageMf.setAppPackageVersion("1.0");
        appPackageMf.setAppClass("vm");
        appPackageMf.setAppType("vm");
        appPackageMf.setAppProviderId("id");
        appPackageMf.setAlgorithm("algorithm");
        appPackageMf.setAppProductName("testProduct");
        appPackageMf.setHash("hash");
        appPackageMf.setAppReleaseDataTime("releaseDate");
        appPackageMf.setSource("source");

        tunnelInfo.setTunnelInfoId("infoId");
        tunnelInfo.setTunnelType("tunnelType");
        tunnelInfo.setTunnelDstAddress("destAddress");
        tunnelInfo.setTunnelSpecificData("date");
        tunnelInfo.setTunnelSrcAddress("sourceAddress");

        dstInterface.setDstInterfaceId("dstId");
        dstInterface.setInterfaceType("dstType");
        dstInterface.setDstIpAddress("1.1.1.1");
        dstInterface.setDstMacAddress("address");
        dstInterface.setTenantId("tenantId");
        dstInterface.setSrcMacAddress("2.2.1.1");
        dstInterface.setTunnelInfo(tunnelInfo);

        dependency.setAppInstanceId("app_instance_id");
        dependency.setDependencyAppInstanceId("depend_app_instance_id");
        dependency.setTenant("tenant");
        instantiateReq.setAppName("Face_Recognition");
        instantiateReq.setPackageId("pkgId");
        instantiateReq.setHostIp("1.1.1.1");
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("key1", dependency);
        instantiateReq.setParameters(objectMap);
        appRule.setAppSupportMp1(true);
        appRule.setAppName(instantiateReq.getAppName());
        appRuleTask.setTenant(dependency.getTenant());
        appRuleTask.setCreateTime(LocalDateTime.MIN);
        appRuleTask.setUpdateTime(LocalDateTime.MAX);
        serviceRequired.setVersion("1.0");
        serviceRequired.setRequestedPermissions(true);
        response.setProcessInstanceID("ProcessInstanceId");

    }

    @Test
    public void testAppInstanceInfoDto() {
        Assert.assertEquals("appPkgDes", appPackageMf.getAppPackageDescription());
        Assert.assertEquals("1.0", appPackageMf.getAppPackageVersion());
        Assert.assertEquals("vm", appPackageMf.getAppClass());
        Assert.assertEquals("vm", appPackageMf.getAppType());
        Assert.assertEquals("id", appPackageMf.getAppProviderId());
        Assert.assertEquals("algorithm", appPackageMf.getAlgorithm());
        Assert.assertEquals("testProduct", appPackageMf.getAppProductName());
        Assert.assertEquals("hash", appPackageMf.getHash());
        Assert.assertEquals("releaseDate", appPackageMf.getAppReleaseDataTime());
        Assert.assertEquals("source", appPackageMf.getSource());

        Assert.assertEquals("dstId", dstInterface.getDstInterfaceId());
        Assert.assertEquals("dstType", dstInterface.getInterfaceType());
        Assert.assertEquals("1.1.1.1", dstInterface.getDstIpAddress());
        Assert.assertEquals("address", dstInterface.getDstMacAddress());
        Assert.assertEquals("tenantId", dstInterface.getTenantId());
        Assert.assertEquals("2.2.1.1", dstInterface.getSrcMacAddress());
        Assert.assertEquals(tunnelInfo, dstInterface.getTunnelInfo());

        Assert.assertEquals("infoId", tunnelInfo.getTunnelInfoId());
        Assert.assertEquals("tunnelType", tunnelInfo.getTunnelType());
        Assert.assertEquals("destAddress", tunnelInfo.getTunnelDstAddress());
        Assert.assertEquals("date", tunnelInfo.getTunnelSpecificData());
        Assert.assertEquals("sourceAddress", tunnelInfo.getTunnelSrcAddress());

        Assert.assertEquals("app_instance_id", dependency.getAppInstanceId());
        Assert.assertEquals("depend_app_instance_id", dependency.getDependencyAppInstanceId());
        Assert.assertEquals("1.1.1.1", instantiateReq.getHostIp());
        Assert.assertNotNull(instantiateReq.getParameters());
        Assert.assertEquals("pkgId", instantiateReq.getPackageId());
        Assert.assertNotNull(instantiateReq.toString());
        Assert.assertEquals("Face_Recognition", appRule.getAppName());
        Assert.assertEquals(true, appRule.getAppSupportMp1());
        Assert.assertNotNull(appRule.toString());
        Assert.assertNotNull(appRuleTask.getUpdateTime());
        Assert.assertNotNull(appRuleTask.getCreateTime());
        Assert.assertEquals("tenant", appRuleTask.getTenant());
        Assert.assertNotNull(appRuleTask.toString());
        Assert.assertEquals("1.0", serviceRequired.getVersion());
        Assert.assertEquals(true, serviceRequired.getRequestedPermissions());
        Assert.assertNotNull(serviceRequired.toString());
        Assert.assertEquals("ProcessInstanceId", response.getProcessInstanceID());
        Assert.assertNotNull(response.toString());

    }
}
