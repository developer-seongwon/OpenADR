package com.avob.openadr.server.oadr20b.ven;

import jakarta.xml.bind.JAXBException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.xml.sax.SAXException;

import com.avob.openadr.client.xmpp.oadr20b.OadrXmppException;
import com.avob.openadr.model.oadr20b.Oadr20bJAXBContext;
import com.avob.openadr.server.oadr20b.ven.xmpp.XmppClientFactory;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "com.avob.openadr.server.oadr20b.ven" })
@PropertySource("classpath:application.properties")
@ActiveProfiles("test")
public class VEN20bApplicationTest {

	
	@Bean
	@Profile({ "test" })
	public Oadr20bJAXBContext jaxbContextTest() throws JAXBException, SAXException {
		return Oadr20bJAXBContext.getInstance("src/main/resources/oadr20b_schema/");
	};
	
	/**
	 * 테스트에서는 XMPP 서버에 붙지 않는다.
	 *
	 * MultiVtnConfigTest 가 "XMPP 설정은 세션으로 등록되지 않는다" 를 단언한다.
	 * 예전에는 그 실패를 만들려고 진짜 서버에 붙어보고 타임아웃을 기다렸다.
	 * 도커 스택이 떠 있으면 openfire 가 5222 를 잡고 /etc/hosts 가 vtn.oadr.com 을
	 * 127.0.0.1 로 보내기 때문에, 실제로 붙어서 5초를 버리고 스택트레이스를 쏟았다.
	 * 더 나쁜 건 어쩌다 연결이 성공하면 테스트가 깨진다는 것이다.
	 *
	 * 여기서 생성 자체를 막으니 소켓이 아예 열리지 않는다. 주변 상태와 무관하다.
	 * 빈 이름을 다르게 둬서 DefaultXmppClientFactory 와 부딪히지 않게 하고,
	 * @Primary 로 이쪽이 주입되게 한다.
	 */
	@Bean
	@Primary
	public XmppClientFactory failingXmppClientFactory() {
		return builder -> {
			throw new OadrXmppException("테스트에서는 XMPP 서버에 붙지 않는다");
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(VEN20bApplicationConfig.class, args);
	}

}
