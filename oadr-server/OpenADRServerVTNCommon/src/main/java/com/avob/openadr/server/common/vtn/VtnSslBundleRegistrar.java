package com.avob.openadr.server.common.vtn;

import java.security.KeyStore;
import java.util.UUID;

import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.boot.ssl.SslStoreBundle;

import com.avob.openadr.security.OadrPKISecurity;
import com.avob.openadr.server.common.vtn.exception.OadrVTNInitializationException;

/**
 * VTN 의 상호 TLS 재료를 스프링 SSL 번들로 등록한다.
 *
 * 예전에는 Jetty 전용 커스터마이저가 VtnConfig 가 만든 SSLContext 를 커넥터에 직접 꽂았다.
 * 그 방식은 서블릿 컨테이너에 코드가 묶인다. Boot 3.1 부터 생긴 SSL 번들을 쓰면
 * 키스토어와 트러스트스토어만 넘기고 커넥터 구성은 프레임워크가 알아서 한다.
 * 컨테이너가 톰캣이든 Jetty 든 코드가 같고, Boot 4 로 올라가도 이 영역은 그대로다.
 *
 * VtnConfig 의 SSLContext(getSslContext)는 지우지 않았다. 지금은 쓰는 곳이 없다.
 * XMPP 는 smack 4.5 로 올리면서 SSLContext 대신 키 매니저, 트러스트 매니저를 받는다.
 * 여기서는 같은 PEM 경로를 다시 읽어 키스토어 객체를 만든다.
 * 톰캣 커넥터 설정이 번들의 KeyStore 를 그대로 받아 쓰기 때문이다.
 *
 * 키와 인증서가 설정되지 않은 경우가 있다. HTTP 만 쓰는 구성인데,
 * 그때는 번들을 등록하지 않는다. VtnConfig 도 같은 조건에서 SSLContext 를 안 만든다.
 */
public class VtnSslBundleRegistrar implements SslBundleRegistrar {

	/** server.ssl.bundle 에서 이 이름으로 참조한다 */
	public static final String BUNDLE_NAME = "oadr-vtn";

	private final VtnConfig vtnConfig;
	private final String[] protocols;
	private final String[] ciphers;

	public VtnSslBundleRegistrar(VtnConfig vtnConfig, String[] protocols, String[] ciphers) {
		this.vtnConfig = vtnConfig;
		this.protocols = protocols;
		this.ciphers = ciphers;
	}

	@Override
	public void registerBundles(SslBundleRegistry registry) {

		if (vtnConfig.getKey() == null || vtnConfig.getCert() == null) {
			return;
		}

		// 키스토어는 메모리에만 있고 파일로 떨어지지 않는다. 비밀번호는 매 기동마다 새로 만든다
		String keystorePassword = UUID.randomUUID().toString();

		try {
			KeyStore keyStore = OadrPKISecurity.createKeyStore(vtnConfig.getKey(), vtnConfig.getCert(),
					keystorePassword);
			KeyStore trustStore = OadrPKISecurity.createTrustStore(vtnConfig.getTrustCertificates());

			SslStoreBundle stores = SslStoreBundle.of(keyStore, keystorePassword, trustStore);

			// OadrPKISecurity.createKeyStore 가 넣는 키 별칭이 "key" 다
			SslBundleKey key = SslBundleKey.of(keystorePassword, "key");

			// 프로토콜과 암호 스위트는 OpenADR 프로파일이 정해 준다.
			// 20a 와 20b 가 서로 다르므로 호출하는 모듈이 넘긴다
			SslOptions options = SslOptions.of(ciphers, protocols);

			registry.registerBundle(BUNDLE_NAME, SslBundle.of(stores, key, options, "TLS"));

		} catch (Exception e) {
			throw new OadrVTNInitializationException(e);
		}
	}

}
