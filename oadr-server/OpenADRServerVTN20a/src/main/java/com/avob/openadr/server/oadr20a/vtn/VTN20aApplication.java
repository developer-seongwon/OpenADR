package com.avob.openadr.server.oadr20a.vtn;

import jakarta.annotation.Resource;
import jakarta.xml.bind.JAXBException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.xml.sax.SAXException;

import com.avob.openadr.model.oadr20a.Oadr20aJAXBContext;
import com.avob.openadr.model.oadr20a.Oadr20aSecurity;
import com.avob.openadr.security.exception.OadrSecurityException;
import com.avob.openadr.server.common.vtn.VTNEmbeddedServletContainerCustomizer;
import com.avob.openadr.server.common.vtn.VtnSslBundleRegistrar;
import com.avob.openadr.server.common.vtn.VtnConfig;

@Configuration
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
@ComponentScan(basePackages = { "com.avob.openadr.server.common.vtn", "com.avob.openadr.server.oadr20a.vtn" })
@EnableJpaRepositories({ "com.avob.openadr.server.common.vtn", "com.avob.openadr.server.oadr20a.vtn" })
@EntityScan({ "com.avob.openadr.server.common.vtn", "com.avob.openadr.server.oadr20a.vtn" })
public class VTN20aApplication {

	@Resource
	private VtnConfig vtnConfig;

	/**
	 * 상호 TLS 재료를 SSL 번들로 등록한다.
	 * 프로토콜과 암호 스위트는 OpenADR 2.0a 프로파일이 정한 목록이다.
	 */
	@Bean
	public SslBundleRegistrar vtnSslBundleRegistrar() {
		return new VtnSslBundleRegistrar(vtnConfig, Oadr20aSecurity.getProtocols(), Oadr20aSecurity.getCiphers());
	}

	/**
	 * 포트와 컨텍스트 경로는 oadr 프로퍼티에서 오고, TLS 는 위 번들을 이름으로 가리킨다.
	 * 키나 인증서가 없는 HTTP 전용 구성이면 번들이 등록되지 않으므로 ssl 도 걸지 않는다.
	 */
	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> servletContainerCustomizer() {
		boolean sslEnabled = vtnConfig.getKey() != null && vtnConfig.getCert() != null;
		return new VTNEmbeddedServletContainerCustomizer(vtnConfig.getPort(), vtnConfig.getContextPath(), sslEnabled);
	}

	@Bean
	@Profile({ "!test" })
	public Oadr20aJAXBContext jaxbContextProd() throws OadrSecurityException, JAXBException {
		if (vtnConfig.getValidateOadrPayloadAgainstXsd()
				&& vtnConfig.getValidateOadrPayloadAgainstXsdFilePath() != null) {
			return Oadr20aJAXBContext.getInstance(vtnConfig.getValidateOadrPayloadAgainstXsdFilePath());
		}
		return Oadr20aJAXBContext.getInstance();
	};

	@Bean
	@Profile({ "test" })
	public Oadr20aJAXBContext jaxbContextTest() throws JAXBException, SAXException {
		return Oadr20aJAXBContext.getInstance();
	};

	public static void main(String[] args) {
		SpringApplication.run(VTN20aApplication.class, args);
	}
}
