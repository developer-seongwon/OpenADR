package com.avob.openadr.server.common.vtn.service;

import com.avob.openadr.server.common.vtn.AbstractVtnTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.avob.openadr.server.common.vtn.ApplicationTest;
import com.avob.openadr.server.common.vtn.models.user.OadrUser;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationTest.class })
@WebAppConfiguration
@ActiveProfiles("test")
public class OadrUserServiceTest extends AbstractVtnTest {

	@Resource
	private OadrUserService oadrUserService;

	@Test
	public void test() {
		String username = "user";
		OadrUser user = oadrUserService.prepare(username);
		oadrUserService.save(user);

		OadrUser findByUsername = oadrUserService.findByUsername(username);
		assertNotNull(findByUsername);
		assertEquals(username, findByUsername.getUsername());
		assertNull(findByUsername.getBasicPassword());
		assertNull(findByUsername.getDigestPassword());

		oadrUserService.delete(findByUsername);
		findByUsername = oadrUserService.findByUsername(username);
		assertNull(findByUsername);

		user = oadrUserService.prepare(username, "mouaiccool");
		oadrUserService.save(user);

		findByUsername = oadrUserService.findByUsername(username);
		assertNotNull(findByUsername);
		assertEquals(username, findByUsername.getUsername());
		assertNotNull(findByUsername.getBasicPassword());
		assertNotNull(findByUsername.getDigestPassword());

		oadrUserService.delete(Arrays.asList(findByUsername));
		findByUsername = oadrUserService.findByUsername(username);
		assertNull(findByUsername);

	}

}
