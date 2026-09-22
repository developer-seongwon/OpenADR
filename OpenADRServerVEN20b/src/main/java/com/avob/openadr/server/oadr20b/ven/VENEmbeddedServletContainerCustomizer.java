package com.avob.openadr.server.oadr20b.ven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Map;
import java.util.UUID;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfigCertificate.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

import com.avob.openadr.security.OadrPKISecurity;

/**
 * VEN 의 내장 웹서버를 구성한다.
 *
 * VEN 은 VTN 세션마다 포트가 따로다. VTN 이 그 포트로 푸시를 보내기 때문이다.
 * 그래서 커넥터를 세션 수만큼 만들어야 하고, 이 부분만은 컨테이너에 묶인다.
 * VTN 쪽은 커넥터가 하나라 SSL 번들로 컨테이너 중립으로 처리했지만 여기는 그럴 수 없다.
 *
 * 예전에는 Jetty 커스터마이저가 세션마다 SslContextFactory 에 이미 만들어진
 * SSLContext 를 꽂았다. 톰캣은 그 주입을 공개 API 로 열어 두지 않아서,
 * 같은 PEM 에서 키스토어를 만들어 임시 파일로 떨구고 그 경로를 준다.
 *
 * 한 가지 동작이 달라진다. Jetty 에서는 커넥터마다 컨텍스트 경로를 다르게 줬는데
 * 톰캣에서 컨텍스트 경로는 커넥터가 아니라 컨텍스트 단위라 그렇게 못 한다.
 * 첫 세션의 경로를 전체에 적용하고, 세션마다 경로가 다르면 경고를 남긴다.
 * 실제 설정에서는 모든 세션이 같은 경로를 쓴다.
 */
public class VENEmbeddedServletContainerCustomizer
		implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VENEmbeddedServletContainerCustomizer.class);

	private final Map<String, VtnSessionConfiguration> multiConfig;
	private final String[] protocols;
	private final String[] ciphers;

	public VENEmbeddedServletContainerCustomizer(Map<String, VtnSessionConfiguration> multiConfig, String[] protocols,
			String[] ciphers) {
		this.multiConfig = multiConfig;
		this.protocols = protocols;
		this.ciphers = ciphers;
	}

	@Override
	public void customize(TomcatServletWebServerFactory factory) {

		factory.setRegisterDefaultServlet(false);

		boolean first = true;
		String contextPath = null;

		for (VtnSessionConfiguration session : multiConfig.values()) {

			if (first) {
				factory.setPort(session.getPort());
				contextPath = session.getContextPath();
				if (contextPath != null && !"".equals(contextPath.trim())) {
					factory.setContextPath(contextPath);
				}
				factory.addConnectorCustomizers(connector -> configureSsl(connector, session));
				first = false;
			} else {
				if (contextPath != null && !contextPath.equals(session.getContextPath())) {
					LOGGER.warn("톰캣은 커넥터별 컨텍스트 경로를 지원하지 않는다. 세션 "
							+ session.getVtnId() + " 의 경로 " + session.getContextPath() + " 대신 "
							+ contextPath + " 가 적용된다");
				}
				Connector connector = new Connector(Http11NioProtocol.class.getName());
				connector.setPort(session.getPort());
				configureSsl(connector, session);
				factory.addAdditionalTomcatConnectors(connector);
			}
		}
	}

	/**
	 * 커넥터 하나에 상호 TLS 를 건다.
	 * 클라이언트 인증은 optional 이다. 인증서를 내면 받고 없어도 연결은 끊지 않는다.
	 * 예전 Jetty 커스터마이저의 setWantClientAuth(true) 와 같은 동작이다.
	 */
	private void configureSsl(Connector connector, VtnSessionConfiguration session) {
		try {
			String password = UUID.randomUUID().toString();

			KeyStore keyStore = OadrPKISecurity.createKeyStore(session.getVenPrivateKeyPath(),
					session.getVenCertificatePath(), password);
			KeyStore trustStore = OadrPKISecurity.createTrustStore(session.getTrustCertificates());

			File keyStoreFile = writeTemporaryKeyStore(keyStore, password, "ven-key");
			File trustStoreFile = writeTemporaryKeyStore(trustStore, password, "ven-trust");

			connector.setScheme("https");
			connector.setSecure(true);
			connector.setProperty("SSLEnabled", "true");

			SSLHostConfig sslHostConfig = new SSLHostConfig();
			sslHostConfig.setSslProtocol("TLS");
			sslHostConfig.setProtocols(String.join(",", protocols));
			sslHostConfig.setCiphers(String.join(",", ciphers));
			sslHostConfig.setCertificateVerification("optional");
			sslHostConfig.setTruststoreFile(trustStoreFile.getAbsolutePath());
			sslHostConfig.setTruststorePassword(password);

			SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, Type.UNDEFINED);
			certificate.setCertificateKeystoreFile(keyStoreFile.getAbsolutePath());
			certificate.setCertificateKeystorePassword(password);
			// OadrPKISecurity.createKeyStore 가 넣는 키 별칭이 "key" 다
			certificate.setCertificateKeyAlias("key");
			certificate.setCertificateKeyPassword(password);
			sslHostConfig.addCertificate(certificate);

			connector.addSslHostConfig(sslHostConfig);

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 톰캣은 키스토어를 파일 경로로만 받는다. 프로세스가 끝나면 지워지는 임시 파일에 쓴다.
	 * 비밀번호는 기동할 때마다 새로 만드는 값이라 파일이 남아도 재사용되지 않는다.
	 */
	private File writeTemporaryKeyStore(KeyStore keyStore, String password, String prefix) throws Exception {
		File file = File.createTempFile(prefix, ".p12");
		file.deleteOnExit();
		try (OutputStream out = new FileOutputStream(file)) {
			keyStore.store(out, password.toCharArray());
		}
		return file;
	}

}
