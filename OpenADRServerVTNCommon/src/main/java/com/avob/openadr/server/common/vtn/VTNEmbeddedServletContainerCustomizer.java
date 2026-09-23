package com.avob.openadr.server.common.vtn;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;

/**
 * VTN 의 내장 웹서버를 구성한다.
 *
 * 예전에는 JettyServletWebServerFactory 로 타입이 박혀 있어서 컨테이너를 바꾸면 이 클래스가 깨졌다.
 * 지금은 ConfigurableServletWebServerFactory 만 쓰기 때문에 톰캣이든 Jetty 든 그대로 돈다.
 *
 * TLS 재료는 VtnSslBundleRegistrar 가 등록한 SSL 번들에서 온다. 여기서는 이름으로 가리키기만 한다.
 * 클라이언트 인증은 WANT 다. 인증서를 내면 받고 없어도 연결은 끊지 않는다.
 * 예전 Jetty 커스터마이저의 setWantClientAuth(true) 와 같은 동작이다.
 * OpenADR 은 인증서 없이 붙는 전송도 허용하므로 NEED 로 올리면 안 된다.
 */
public class VTNEmbeddedServletContainerCustomizer
		implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	private final int port;
	private final String contextPath;
	private final boolean sslEnabled;

	public VTNEmbeddedServletContainerCustomizer(int port, String contextPath, boolean sslEnabled) {
		this.port = port;
		this.contextPath = contextPath;
		this.sslEnabled = sslEnabled;
	}

	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {

		if (contextPath != null && !"".equals(contextPath.trim())) {
			factory.setContextPath(contextPath);
		}

		configure(factory);
	}

	private void configure(ConfigurableWebServerFactory factory) {
		factory.setPort(port);

		if (sslEnabled) {
			Ssl ssl = Ssl.forBundle(VtnSslBundleRegistrar.BUNDLE_NAME);
			ssl.setClientAuth(Ssl.ClientAuth.WANT);
			factory.setSsl(ssl);
		}
	}

}
