package org.edgegallery.mecm.appo.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class AppoModelTest {
    @InjectMocks
    AppPackageMf appPackageMf = new AppPackageMf();
    DstInterface dstInterface = new DstInterface();
    TunnelInfo tunnelInfo = new TunnelInfo();

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

    }

}
