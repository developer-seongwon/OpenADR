package com.avob.openadr.server.oadr20b.ven;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.avob.openadr.model.oadr20b.Oadr20bJAXBContext;
import com.avob.openadr.model.oadr20b.Oadr20bSecurity;
import com.avob.openadr.security.exception.OadrSecurityException;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "com.avob.openadr.server.oadr20b.ven" })
public class VEN20bApplicationConfig {

	@Value("${oadr.security.validateOadrPayloadAgainstXsdFilePath:#{null}}")
	private String validateOadrPayloadAgainstXsdFilePath;

	@Bean
	@Profile("!test")
	public Oadr20bJAXBContext jaxbContextProd() throws OadrSecurityException, JAXBException {
		if (validateOadrPayloadAgainstXsdFilePath != null) {
			return Oadr20bJAXBContext.getInstance(validateOadrPayloadAgainstXsdFilePath);
		}
		return Oadr20bJAXBContext.getInstance();
	};

	@Bean
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newScheduledThreadPool(5);
	}

	@Bean
	public ScheduledExecutorService eiEventExecutorService() {
		return Executors.newScheduledThreadPool(5);
	}

	// MultiVtnConfig 를 필드로 받으면 이 설정 클래스 자체가 MultiVtnConfig 에 기대게 된다.
	// MultiVtnConfig -> VtnSessionFactory -> 여기서 만드는 jaxbContextProd 로 다시 돌아와서 순환 참조가 된다.
	// 쓰는 곳이 이 빈 하나뿐이라 메서드 인자로 받는다
	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer(
			MultiVtnConfig multiVtnConfig) {
		return new VENEmbeddedServletContainerCustomizer(multiVtnConfig.getMultiConfig(),
				Oadr20bSecurity.getProtocols(), Oadr20bSecurity.getCiphers());
	}

}
