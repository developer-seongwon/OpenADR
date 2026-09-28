package com.avob.openadr.server.oadr20b.vtn;

import java.io.IOException;

import jakarta.annotation.Resource;
import jakarta.xml.bind.JAXBException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JndiDataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.XADataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.rsocket.RSocketSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.xml.sax.SAXException;

import com.avob.openadr.model.oadr20b.Oadr20bJAXBContext;
import com.avob.openadr.model.oadr20b.Oadr20bSecurity;
import com.avob.openadr.security.exception.OadrSecurityException;
import com.avob.openadr.server.common.vtn.VTNEmbeddedServletContainerCustomizer;
import com.avob.openadr.server.common.vtn.VtnSslBundleRegistrar;
import com.avob.openadr.server.common.vtn.VtnConfig;

@SpringBootApplication
@EnableJms
@Configuration
// 자동설정 제외 목록.
//
// 예전에는 90개가 넘었는데 카산드라, 몽고, 레디스, 카프카, 웹플럭스처럼 이 프로젝트에
// 의존조차 없는 것들이 대부분이었다. 부트는 @ConditionalOnClass 가 안 맞으면 어차피
// 적용하지 않으므로 그 제외들은 아무 일도 하지 않았고, 부트가 클래스를 옮기거나 지울
// 때마다 컴파일만 깨뜨렸다. 부트 4 로 올리면서 클래스패스에 실제로 존재하는 것만 남겼다.
@EnableAutoConfiguration(exclude = {
		AopAutoConfiguration.class,
		JndiDataSourceAutoConfiguration.class,
		MessageSourceAutoConfiguration.class,
		ProjectInfoAutoConfiguration.class,
		RSocketSecurityAutoConfiguration.class,
		ReactiveUserDetailsServiceAutoConfiguration.class,
		SecurityAutoConfiguration.class,
		TaskExecutionAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		WebMvcAutoConfiguration.class,
		XADataSourceAutoConfiguration.class })
@ComponentScan(basePackages = { "com.avob.openadr.server.common.vtn", "com.avob.openadr.server.oadr20b.vtn" })
@EnableJpaRepositories({ "com.avob.openadr.server.common.vtn", "com.avob.openadr.server.oadr20b.vtn" })
@EntityScan({ "com.avob.openadr.server.common.vtn", "com.avob.openadr.server.oadr20b.vtn" })
public class VTN20bApplication {

	@Resource
	private VtnConfig vtnConfig;

	/**
	 * 상호 TLS 재료를 SSL 번들로 등록한다.
	 * 프로토콜과 암호 스위트는 OpenADR 2.0b 프로파일이 정한 목록이다.
	 */
	@Bean
	public SslBundleRegistrar vtnSslBundleRegistrar() {
		return new VtnSslBundleRegistrar(vtnConfig, Oadr20bSecurity.getProtocols(), Oadr20bSecurity.getCiphers());
	}

	/**
	 * 포트와 컨텍스트 경로는 oadr 프로퍼티에서 오고, TLS 는 위 번들을 이름으로 가리킨다.
	 * 컨테이너 타입에 묶이지 않아서 톰캣이든 Jetty 든 그대로 돈다.
	 */
	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> servletContainerCustomizer() {
		boolean sslEnabled = vtnConfig.getKey() != null && vtnConfig.getCert() != null;
		return new VTNEmbeddedServletContainerCustomizer(vtnConfig.getPort(), vtnConfig.getContextPath(), sslEnabled);
	}

	@Bean
	@Profile({ "!test" })
	public Oadr20bJAXBContext jaxbContextProd() throws OadrSecurityException, JAXBException {
		if (vtnConfig.getValidateOadrPayloadAgainstXsd()
				&& vtnConfig.getValidateOadrPayloadAgainstXsdFilePath() != null) {
			return Oadr20bJAXBContext.getInstance(vtnConfig.getValidateOadrPayloadAgainstXsdFilePath());
		}
		return Oadr20bJAXBContext.getInstance();
	};

	@Bean
	@Profile({ "test" })
	public Oadr20bJAXBContext jaxbContextTest() throws JAXBException, SAXException {
		return Oadr20bJAXBContext.getInstance();
	};

	public static void main(String[] args) throws IOException {
		SpringApplication.run(VTN20bApplication.class, args);
	}
}
