package org.edgegallery.mecm.appo.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class DnsRuleTest {

    @InjectMocks
    DnsRule dnsRule = new DnsRule();

    @Before
    public void setUp() {
        dnsRule.setDnsRuleId("dnsRuleId");
        dnsRule.setDomainName("domainName");
        dnsRule.setIpAddressType("ipAddressType");
        dnsRule.setIpAddress("ipAddress");
        dnsRule.setTtl(1);
    }

    @Test
    public void testDnsRule() {
        Assert.assertEquals("dnsRuleId", dnsRule.getDnsRuleId());
        Assert.assertEquals("domainName", dnsRule.getDomainName());
        Assert.assertEquals("ipAddressType", dnsRule.getIpAddressType());
        Assert.assertEquals("ipAddress", dnsRule.getIpAddress());
        Assert.assertEquals(1, dnsRule.getTtl());
        Assert.assertNotNull(dnsRule.toString());

    }
}
