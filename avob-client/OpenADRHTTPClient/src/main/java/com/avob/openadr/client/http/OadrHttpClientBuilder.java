package com.avob.openadr.client.http;

import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import com.avob.openadr.security.OadrPKISecurity;
import com.avob.openadr.security.exception.OadrSecurityException;

/**
 * {@link OadrHttpClient} 를 만든다. 공개 메서드는 Apache httpclient 4 시절과 같다.
 *
 * 안에서는 자바 표준 java.net.http.HttpClient 를 조립한다. Apache 의 기본 동작 중
 * 결과에 영향을 주는 것들은 똑같이 맞췄다.
 *
 * - HTTP/1.1 고정. 자바 HttpClient 는 기본으로 HTTP/2 를 먼저 시도한다
 * - 프록시 안 씀. Apache HttpClientBuilder.create() 는 시스템 프록시 설정을 읽지 않았다
 * - 쿠키 유지. Apache 는 기본 쿠키 저장소가 있어서 서버가 준 세션 쿠키를 다음 요청에 실었다
 * - 리다이렉트 안 따라감. Apache 는 POST 의 리다이렉트를 따라가지 않았다
 */
public class OadrHttpClientBuilder {

	/**
	 * protocol config
	 */
	private String[] protocols;
	private String[] ciphers;

	/**
	 * SSL trusted certificate
	 */
	private List<String> trustedCertificateFilePath;

	/**
	 * default x509 client certificate
	 */
	private String clientPrivateKeyPemFilePath;
	private String clientCertificatePemFilePath;

	/**
	 * default Digest/Basic authentication. 둘 중 나중에 부른 것 하나만 남는다(예전과 같다)
	 */
	private URI authTarget;
	private String basicAuthorization;
	private DigestAuthenticator digest;

	/**
	 * pooling client
	 */
	private Integer totalConnection;
	private Integer totalPerRouteConnection;

	/**
	 * timeout
	 */
	private Duration connectTimeout;

	/**
	 * default host
	 */
	private URI defaultUri;

	/**
	 * default http headers. 같은 이름을 여러 번 줄 수 있어서 Map 이 아니라 목록이다
	 */
	private final List<Map.Entry<String, String>> headers = new ArrayList<>();

	/**
	 * enable http
	 */
	private boolean enableHttp = false;

	public OadrHttpClientBuilder withTrustedCertificate(List<String> trustedCertificateFilePath) {
		this.trustedCertificateFilePath = trustedCertificateFilePath;
		return this;
	}

	public OadrHttpClientBuilder withProtocol(String[] protocols, String[] ciphers) {
		this.protocols = protocols;
		this.ciphers = ciphers;
		return this;
	}

	public OadrHttpClientBuilder withX509Authentication(String clientPrivateKeyPemFilePath,
			String clientCertificatePemFilePath) throws OadrSecurityException {
		this.clientPrivateKeyPemFilePath = clientPrivateKeyPemFilePath;
		this.clientCertificatePemFilePath = clientCertificatePemFilePath;
		return this;
	}

	public static String buildNonce(String key) {
		// expirationTime + ":" + md5Hex(expirationTime + ":" + key)
		String dateTimeString = Long.toString(OffsetDateTime.now().plusMinutes(5).toEpochSecond() * 1000);
		String nonce = dateTimeString + ":" + OadrPKISecurity.md5Hex(dateTimeString + ":" + key);
		return Base64.getEncoder().encodeToString(nonce.getBytes());
	}

	/**
	 * Digest 인증. realm 과 key 로 만든 nonce 로 첫 요청부터 헤더를 붙이고,
	 * 서버가 새 nonce 를 주면 그걸로 다시 보낸다. 자세한 건 {@link DigestAuthenticator}
	 */
	public OadrHttpClientBuilder withDefaultDigestAuthentication(String host, String realm, String key, String username,
			String password) throws OadrSecurityException {
		this.withDefaultHost(host);
		this.authTarget = defaultUri;
		this.basicAuthorization = null;
		this.digest = new DigestAuthenticator(username, password, realm, buildNonce(key));
		return this;
	}

	/**
	 * Basic 인증. 챌린지를 기다리지 않고 첫 요청부터 헤더를 붙인다(Apache 의 AuthCache 선제 인증과 같다)
	 */
	public OadrHttpClientBuilder withDefaultBasicAuthentication(String host, String username, String password)
			throws OadrSecurityException {
		this.withDefaultHost(host);
		this.authTarget = defaultUri;
		this.digest = null;
		String token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		this.basicAuthorization = "Basic " + token;
		return this;
	}

	/**
	 * setup default header
	 * 
	 * @param header
	 * @param value
	 * @return
	 */
	public OadrHttpClientBuilder withHeader(String header, String value) {
		headers.add(new AbstractMap.SimpleImmutableEntry<>(header, value));
		return this;
	}

	/**
	 * set default host
	 * 
	 * @param host
	 * @return
	 * @throws URISyntaxException
	 */
	public OadrHttpClientBuilder withDefaultHost(String defaultHost) throws OadrSecurityException {
		try {
			defaultUri = new URI(defaultHost);
			if (defaultUri.getHost() == null) {
				throw new OadrSecurityException("given url must specify target host");
			}
		} catch (URISyntaxException e) {
			throw new OadrSecurityException(e);
		}
		return this;
	}

	/**
	 * 동시에 보낼 수 있는 요청 수.
	 *
	 * 예전에는 Apache 연결 풀의 전체 연결 수와 호스트당 연결 수였다. 자바 HttpClient 는 연결 풀 크기를
	 * 클라이언트별로 정할 수 없어서, 둘 중 작은 값만큼만 동시에 보내도록 묶는다.
	 * 안 부르면 1 이다. 예전 Apache 의 기본(BasicHttpClientConnectionManager)이 연결 하나짜리였다
	 */
	public OadrHttpClientBuilder withPooling(int totalConnection, int totalPerRouteConnection) {
		this.totalConnection = totalConnection;
		this.totalPerRouteConnection = totalPerRouteConnection;
		return this;
	}

	/**
	 * 연결 타임아웃. 예전 RequestConfig.setConnectTimeout 과 같이 연결을 맺는 시간만 제한한다
	 */
	public OadrHttpClientBuilder withTimeout(int timeoutMilli) {
		this.connectTimeout = Duration.ofMillis(timeoutMilli);
		return this;
	}

	public OadrHttpClientBuilder enableHttp(boolean enable) {
		this.enableHttp = enable;
		return this;
	}

	public OadrHttpClient build() throws OadrSecurityException {
		String password = UUID.randomUUID().toString();
		SSLContext sc = OadrPKISecurity.createSSLContext(clientPrivateKeyPemFilePath, clientCertificatePemFilePath,
				this.trustedCertificateFilePath, password);

		// 프로토콜과 암호 스위트를 안 주면 SSLContext 기본값을 쓴다(예전 SSLConnectionSocketFactory 에 null 을 준 것과 같다).
		// 호스트 이름 검증은 자바 HttpClient 가 기본으로 켠다(예전 DefaultHostnameVerifier 와 같다)
		SSLParameters sslParameters = sc.getDefaultSSLParameters();
		if (protocols != null) {
			sslParameters.setProtocols(protocols);
		}
		if (ciphers != null) {
			sslParameters.setCipherSuites(ciphers);
		}

		HttpClient.Builder builder = HttpClient.newBuilder().sslContext(sc).sslParameters(sslParameters)
				.version(HttpClient.Version.HTTP_1_1).proxy(HttpClient.Builder.NO_PROXY)
				.cookieHandler(new CookieManager()).followRedirects(HttpClient.Redirect.NEVER);
		if (connectTimeout != null) {
			builder.connectTimeout(connectTimeout);
		}

		int maxConcurrentRequests = 1;
		if (totalConnection != null && totalPerRouteConnection != null) {
			maxConcurrentRequests = Math.max(1, Math.min(totalConnection, totalPerRouteConnection));
		}

		return new OadrHttpClient(builder.build(), defaultUri, headers, enableHttp, maxConcurrentRequests, authTarget,
				basicAuthorization, digest);
	}
}
