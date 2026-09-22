package com.avob.openadr.server.oadr20a.vtn.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;

import jakarta.annotation.Resource;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.avob.openadr.client.http.oadr20a.vtn.OadrHttpVtnClient20a;
import com.avob.openadr.model.oadr20a.builders.Oadr20aBuilders;
import com.avob.openadr.model.oadr20a.exception.Oadr20aException;
import com.avob.openadr.model.oadr20a.exception.Oadr20aHttpLayerException;
import com.avob.openadr.model.oadr20a.oadr.OadrDistributeEvent;
import com.avob.openadr.model.oadr20a.oadr.OadrResponse;
import com.avob.openadr.server.oadr20a.vtn.VTN20aSecurityApplicationTest;
import com.avob.openadr.server.oadr20a.vtn.service.push.Oadr20aPushService;

/*
 * 테스트 클래스마다 컨텍스트를 새로 띄운다.
 *
 * 예전에는 클래스마다 H2 임베디드 DB 가 따로 생겨서 데이터가 섞일 일이 없었다.
 * 지금은 세 클래스가 컨테이너 하나를 같이 쓰는데, 컨텍스트 설정이 같아서
 * 스프링이 컨텍스트를 캐시하고 재사용한다. 그러면 앞 클래스가 남긴 ven1 같은 행 때문에
 * 뒤 클래스에서 username 유니크 제약에 걸린다.
 *
 * ddl-auto 가 create-drop 이라 컨텍스트가 새로 뜰 때 스키마를 다시 만든다.
 * 이 애노테이션으로 클래스가 끝날 때 컨텍스트를 버리면 다음 클래스는 빈 DB 에서 시작한다.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { VTN20aSecurityApplicationTest.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class Oadr20aDemandResponseEventPushServiceTest {

	@Resource
	private Oadr20aPushService oadr20aPushService;

	@Test
	public void test() throws Oadr20aException, URISyntaxException, Oadr20aHttpLayerException {

		String venUsername = "username";
		String venPushUrl = "https://localhost";

		OadrHttpVtnClient20a mockOadrHttpVtnClient20a = Mockito.mock(OadrHttpVtnClient20a.class);

		OadrResponse mockOadrResponse = Oadr20aBuilders.newOadr20aResponseBuilder("", HttpServletResponse.SC_OK).build();
		when(mockOadrHttpVtnClient20a.oadrDistributeEvent(any(String.class), any(OadrDistributeEvent.class)))
				.thenReturn(mockOadrResponse);

		oadr20aPushService.setOadrHttpVtnClient20a(mockOadrHttpVtnClient20a);

		oadr20aPushService.call(venUsername, venPushUrl);
	}

}
