package com.avob.openadr.server.common.vtn.service;

import com.avob.openadr.server.common.vtn.AbstractVtnTest;


import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.avob.openadr.server.common.vtn.ApplicationTest;
import com.avob.openadr.server.common.vtn.models.ven.VenCreateDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationTest.class })
@WebAppConfiguration
@ActiveProfiles("test")
public class GenerateX509CertificateServiceTest extends AbstractVtnTest {

	@Resource
	private GenerateX509CertificateService generateX509CertificateService;

	@Test
	public void test() {
		assertNotNull(generateX509CertificateService);
		VenCreateDto dto = new VenCreateDto();
		dto.setAuthenticationType("x509");
		dto.setCommonName("myven");
		dto.setNeedCertificateGeneration("rsa");
		dto.setOadrProfil("oadr20b");

	}
}
