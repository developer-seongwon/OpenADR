package com.avob.openadr.server.common.vtn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationTest.class })
@WebAppConfiguration
@ActiveProfiles("test")
public class VtnConfigTest extends AbstractVtnTest {

	@Resource
	private VtnConfig vtnConfig;

	@Test
	public void vtnConfigTest() {
		assertTrue(vtnConfig.hasInMemoryBroker());
		assertFalse(vtnConfig.hasExternalRabbitMQBroker());
	}
}
