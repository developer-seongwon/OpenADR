package com.avob.openadr.server.oadr20b.ven.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletContext;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.avob.openadr.model.oadr20b.Oadr20bJAXBContext;
import com.avob.openadr.model.oadr20b.Oadr20bUrlPath;
import com.avob.openadr.model.oadr20b.errorcodes.Oadr20bApplicationLayerErrorCode;
import com.avob.openadr.model.oadr20b.oadr.OadrResponseType;
import com.avob.openadr.server.oadr20b.ven.VEN20bApplicationTest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { VEN20bApplicationTest.class })
@WebAppConfiguration
@ActiveProfiles("test")
public class Oadr20bVENEiReportControllerTest {

	private static final String EIREPORT_ENDPOINT = Oadr20bUrlPath.OADR_BASE_PATH + Oadr20bUrlPath.EI_REPORT_SERVICE;

	public static final UserRequestPostProcessor VTN_SECURITY_SESSION = SecurityMockMvcRequestPostProcessors.user("vtn")
			.roles("VTN");
	
	private Oadr20bJAXBContext jaxbContext;

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@BeforeEach
	public void setup() throws Exception {
		jaxbContext = Oadr20bJAXBContext.getInstance();
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilters(springSecurityFilterChain).build();
	}

	@Test
	public void givenWac_whenServletContext_thenItProvidesOadr20aVENEiEventController() {
		ServletContext servletContext = wac.getServletContext();
		Assertions.assertNotNull(servletContext);
		Assertions.assertTrue(servletContext instanceof MockServletContext);
		Assertions.assertNotNull(wac.getBean("oadr20bVENEiReportController"));
	}

	@Test
	public void requestTest() throws Exception {
		// GET not allowed
		this.mockMvc.perform(MockMvcRequestBuilders.get(EIREPORT_ENDPOINT).with(VTN_SECURITY_SESSION))
				.andExpect(MockMvcResultMatchers.status().is(HttpServletResponse.SC_METHOD_NOT_ALLOWED));

		// PUT not allowed
		this.mockMvc.perform(MockMvcRequestBuilders.put(EIREPORT_ENDPOINT).with(VTN_SECURITY_SESSION))
				.andExpect(MockMvcResultMatchers.status().is(HttpServletResponse.SC_METHOD_NOT_ALLOWED));

		// DELETE not allowed
		this.mockMvc.perform(MockMvcRequestBuilders.delete(EIREPORT_ENDPOINT).with(VTN_SECURITY_SESSION))
				.andExpect(MockMvcResultMatchers.status().is(HttpServletResponse.SC_METHOD_NOT_ALLOWED));

		// POST without content
		String content = "";
		this.mockMvc.perform(MockMvcRequestBuilders.post(EIREPORT_ENDPOINT).with(VTN_SECURITY_SESSION).content(content))
				.andExpect(MockMvcResultMatchers.status().is(HttpServletResponse.SC_BAD_REQUEST));

		// POST without content
		content = "mouaiccool";
		MvcResult andReturn = this.mockMvc
				.perform(MockMvcRequestBuilders.post(EIREPORT_ENDPOINT)
						.with(VTN_SECURITY_SESSION).content(content))
				.andExpect(MockMvcResultMatchers.status().is(HttpServletResponse.SC_OK)).andReturn();
		OadrResponseType unmarshal = jaxbContext.unmarshal(andReturn.getResponse().getContentAsString(), OadrResponseType.class);
		assertEquals(String.valueOf(Oadr20bApplicationLayerErrorCode.NOT_RECOGNIZED_453),
				unmarshal.getEiResponse().getResponseCode());

	}
}
