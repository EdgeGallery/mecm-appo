package org.edgegallery.mecm.appo.apihandler;

import org.edgegallery.mecm.appo.common.AppoConstantsTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class CreateParamTest {

	@InjectMocks
	CreateParam createParam = new CreateParam();

	@Before
	public void setUp(){
		createParam.setAppdId(AppoConstantsTest.APP_ID);
	}

	@Test
	public void testCreateParam() {
		Assert.assertEquals(AppoConstantsTest.APP_ID,createParam.getAppdId());
	}

}
