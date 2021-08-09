package org.edgegallery.mecm.appo.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
public class TrafficRuleTest {

    @InjectMocks
    TrafficRule trafficRule = new TrafficRule();
    TrafficFilter trafficFilter = new TrafficFilter();

    @Before
    public void setUp() {
        trafficRule.setTrafficRuleId("trafficRuleId");
        trafficRule.setFilterType("filterType");
        trafficRule.setPriority(1);
        trafficRule.setAction("action");

        //trafficFilter.setSrcAddress("srcAddress");
        //trafficFilter.setSrcPort("srcPort");
        //trafficFilter.setDstAddress("dstAddress");
        //trafficFilter.setDstPort("dstPort");
        //trafficFilter.setTag("tag");
        //trafficFilter.setSrcTunnelAddress("srcTunnelAddress");

    }

    @Test
    public void testDnsRule() {
        Assert.assertEquals("trafficRuleId", trafficRule.getTrafficRuleId());
        Assert.assertEquals("filterType", trafficRule.getFilterType());
        Assert.assertEquals("action", trafficRule.getAction());
        Assert.assertNotNull(trafficRule.toString());

        Assert.assertEquals(null, trafficFilter.getSrcAddress());
        Assert.assertEquals(null, trafficFilter.getSrcPort());
        Assert.assertEquals(null, trafficFilter.getDstAddress());
        Assert.assertEquals(null, trafficFilter.getDstPort());
        Assert.assertEquals(null, trafficFilter.getTag());
        Assert.assertEquals(null, trafficFilter.getSrcTunnelAddress());
        Assert.assertNotNull(trafficFilter.toString());

    }
}
