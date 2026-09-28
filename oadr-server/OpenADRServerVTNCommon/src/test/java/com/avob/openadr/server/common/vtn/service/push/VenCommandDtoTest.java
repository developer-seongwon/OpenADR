package com.avob.openadr.server.common.vtn.service.push;

import com.avob.openadr.server.common.vtn.AbstractVtnTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.avob.openadr.server.common.vtn.ApplicationTest;
import com.avob.openadr.server.common.vtn.models.ven.Ven;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationTest.class })
@WebAppConfiguration
@ActiveProfiles("test")
public class VenCommandDtoTest extends AbstractVtnTest {

	@Test
	public void test() {
		Ven ven = new Ven();
		String payload = "mouaiccool";
		String username = "username";
		ven.setUsername(username);
		ven.setXmlSignature(true);
		String pushUrl = "ven1@oadr.com";
		ven.setPushUrl(pushUrl);
		String transport = "xmpp";
		ven.setTransport(transport);
		VenCommandDto<String> venCommandDto = new VenCommandDto<>(ven, payload, String.class);
		assertEquals(username, venCommandDto.getVenUsername());
		assertEquals(pushUrl, venCommandDto.getVenPushUrl());
		assertEquals(transport, venCommandDto.getVenTransport());
		assertEquals(String.class, venCommandDto.getPayloadClass());
		assertEquals(payload, venCommandDto.getPayload());
		assertNotNull(venCommandDto.toString());

	}

}
