package com.avob.openadr.server.oadr20b.vtn.controller;

import com.avob.openadr.server.oadr20b.vtn.AbstractVtn20bTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.avob.openadr.server.common.vtn.VtnConfig;
import com.avob.openadr.server.oadr20b.vtn.VTN20bSecurityApplicationTest;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrDataBaseSetup;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrMockHttpVtnMvc;

@ContextConfiguration(classes = { VTN20bSecurityApplicationTest.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Oadr20bVtnControllerTest extends AbstractVtn20bTest {

	@Resource
	private OadrMockHttpVtnMvc oadrMockHttpVtnMvc;

	@Resource
	private VtnConfig vtnConfig;

	@Test
	public void test() throws Exception {

		VtnConfigurationDto conf = oadrMockHttpVtnMvc.getConfiguration(OadrDataBaseSetup.ADMIN_SECURITY_SESSION,
				HttpServletResponse.SC_OK);

		assertNotNull(conf);

		assertEquals(vtnConfig.getContextPath(), conf.getContextPath());
		assertTrue(vtnConfig.getPort() == conf.getPort());
		assertEquals(vtnConfig.getPullFrequencySeconds(), conf.getPullFrequencySeconds());
		assertEquals(vtnConfig.getSupportPush(), conf.getSupportPush());
		assertEquals(vtnConfig.getSupportUnsecuredHttpPush(), conf.getSupportUnsecuredHttpPush());
		assertEquals(vtnConfig.getReplayProtectAcceptedDelaySecond(), conf.getXmlSignatureReplayProtectSecond());

		oadrMockHttpVtnMvc.getConfiguration(OadrDataBaseSetup.USER_SECURITY_SESSION, HttpServletResponse.SC_FORBIDDEN);
		oadrMockHttpVtnMvc.getConfiguration(OadrDataBaseSetup.VEN_HTTP_PULL_DSIG_SECURITY_SESSION, HttpServletResponse.SC_FORBIDDEN);

	}

}
