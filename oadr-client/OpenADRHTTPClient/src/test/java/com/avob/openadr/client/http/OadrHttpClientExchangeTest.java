package com.avob.openadr.client.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * JDK 내장 HttpServer 로 실제 요청을 주고받아서 OadrHttpClient 가 보내는 것을 확인한다.
 *
 * Apache httpclient 4 에서 자바 표준 HttpClient 로 바꾸면서 직접 만든 부분
 * (Basic, Digest 헤더, Digest 401 재시도, http 차단, Content-Type, 인증 범위)을 여기서 본다.
 * TLS 쪽은 VTN20a, VTN20b 의 보안 테스트가 실제 서버에 x509 로 붙어서 확인한다
 */
public class OadrHttpClientExchangeTest {

	private static final String[] TRUSTED = { "src/test/resources/rsa/TEST_OpenADR_RSA_RCA0002_Cert.pem",
			"src/test/resources/rsa/TEST_OpenADR_RSA_SPCA0002_Cert.pem" };

	private static final String REALM = "digest.oadr.com";
	private static final String SERVER_NONCE = "server-nonce";

	/** 서버가 받은 요청들 */
	private final List<Received> received = new CopyOnWriteArrayList<>();

	private HttpServer server;
	private int port;

	private record Received(String method, String path, String authorization, String contentType, String body) {
	}

	@BeforeEach
	public void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", this::handle);
		server.start();
		port = server.getAddress().getPort();
	}

	@AfterEach
	public void stop() {
		server.stop(0);
	}

	/**
	 * /digest 아래로 오는 요청은 Spring Security 의 DigestAuthenticationFilter 처럼 굴린다.
	 * 서버 nonce 로 계산한 응답이 맞으면 200, 아니면 401 과 새 챌린지를 준다
	 */
	private void handle(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		String authorization = exchange.getRequestHeaders().getFirst("Authorization");
		received.add(new Received(exchange.getRequestMethod(), exchange.getRequestURI().getRawPath(), authorization,
				exchange.getRequestHeaders().getFirst("Content-Type"), body));

		int status = 200;
		if (exchange.getRequestURI().getPath().startsWith("/digest") && !validDigest(exchange, authorization)) {
			status = 401;
			exchange.getResponseHeaders().add("WWW-Authenticate",
					"Digest realm=\"" + REALM + "\", qop=\"auth\", nonce=\"" + SERVER_NONCE + "\", stale=\"true\"");
		}
		byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
	}

	/** 클라이언트 구현과 별개로, 받은 헤더만 보고 RFC 2617 식으로 다시 계산해서 맞춰 본다 */
	private boolean validDigest(HttpExchange exchange, String authorization) {
		Map<String, String> p = DigestAuthenticator.parseChallenge(authorization);
		if (p == null || !SERVER_NONCE.equals(p.get("nonce")) || !"auth".equals(p.get("qop"))) {
			return false;
		}
		String ha1 = DigestAuthenticator.md5Hex(p.get("username") + ":" + REALM + ":pw");
		String ha2 = DigestAuthenticator.md5Hex(exchange.getRequestMethod() + ":" + exchange.getRequestURI().getRawPath());
		String expected = DigestAuthenticator.md5Hex(
				ha1 + ":" + SERVER_NONCE + ":" + p.get("nc") + ":" + p.get("cnonce") + ":auth:" + ha2);
		return expected.equals(p.get("response")) && exchange.getRequestURI().getRawPath().equals(p.get("uri"));
	}

	private String base() {
		return "http://127.0.0.1:" + port;
	}

	@Test
	public void basicAuthenticationIsSentPreemptively() throws Exception {
		OadrHttpClient client = new OadrHttpClientBuilder().withTrustedCertificate(Arrays.asList(TRUSTED))
				.withDefaultBasicAuthentication(base() + "/OpenADR2/Simple", "ven1", "pw").enableHttp(true).build();

		HttpResponse<String> response = client.post("<oadr/>", "/EiEvent");

		assertEquals(200, response.statusCode());
		assertEquals("ok", response.body());
		assertEquals(1, received.size());
		Received r = received.get(0);
		assertEquals("POST", r.method());
		// 기본 호스트의 경로 뒤에 path 가 붙는다
		assertEquals("/OpenADR2/Simple/EiEvent", r.path());
		assertEquals("Basic " + Base64.getEncoder().encodeToString("ven1:pw".getBytes(StandardCharsets.UTF_8)),
				r.authorization());
		assertEquals(OadrHttpClient.DEFAULT_CONTENT_TYPE, r.contentType());
		assertEquals("<oadr/>", r.body());
	}

	@Test
	public void defaultHeaderOverridesContentType() throws Exception {
		OadrHttpClient client = new OadrHttpClientBuilder().withTrustedCertificate(Arrays.asList(TRUSTED))
				.withDefaultHost(base()).withHeader("Content-Type", "application/json").enableHttp(true).build();

		client.post("{}", "/x");

		assertEquals("application/json", received.get(0).contentType());
		assertNull(received.get(0).authorization());
	}

	@Test
	public void digestRetriesOnceWithServerNonceThenReusesIt() throws Exception {
		// key 가 서버와 달라서 미리 만든 nonce 는 거절된다(VEN20b 가 실제로 이렇다)
		OadrHttpClient client = new OadrHttpClientBuilder().withTrustedCertificate(Arrays.asList(TRUSTED))
				.withDefaultDigestAuthentication(base(), REALM, "wrong-key", "ven1", "pw").enableHttp(true).build();

		HttpResponse<String> first = client.post("<a/>", "/digest/EiEvent");
		assertEquals(200, first.statusCode());
		// 선제 인증 한 번(401) + 서버 nonce 로 재시도 한 번(200)
		assertEquals(2, received.size());
		assertEquals(null, DigestAuthenticator.parseChallenge(received.get(0).authorization()).get("qop"));
		assertEquals("00000001", DigestAuthenticator.parseChallenge(received.get(1).authorization()).get("nc"));

		HttpResponse<String> second = client.post("<b/>", "/digest/EiEvent");
		assertEquals(200, second.statusCode());
		// 두 번째부터는 받아 둔 nonce 로 바로 통과한다
		assertEquals(3, received.size());
		assertEquals("00000002", DigestAuthenticator.parseChallenge(received.get(2).authorization()).get("nc"));
	}

	@Test
	public void wrongDigestPasswordGivesUpAfterOneRetry() throws Exception {
		OadrHttpClient client = new OadrHttpClientBuilder().withTrustedCertificate(Arrays.asList(TRUSTED))
				.withDefaultDigestAuthentication(base(), REALM, "", "ven1", "not-pw").enableHttp(true).build();

		assertEquals(401, client.post("<a/>", "/digest/EiEvent").statusCode());
		assertEquals(2, received.size());
	}

	@Test
	public void credentialsAreNotSentToOtherHosts() throws Exception {
		// 기본 호스트는 127.0.0.1 이고, 같은 서버를 localhost 라는 다른 이름으로 부르면 인증 헤더가 안 붙어야 한다
		OadrHttpClient client = new OadrHttpClientBuilder().withTrustedCertificate(Arrays.asList(TRUSTED))
				.withDefaultBasicAuthentication(base(), "ven1", "pw").enableHttp(true).build();

		client.post("<a/>", "http://localhost:" + port, "/push");

		assertEquals("/push", received.get(0).path());
		assertNull(received.get(0).authorization());
	}

	@Test
	public void httpIsRefusedUnlessEnabled() throws Exception {
		OadrHttpClient client = new OadrHttpClientBuilder().withTrustedCertificate(Arrays.asList(TRUSTED))
				.withDefaultHost(base()).build();

		assertThrows(IOException.class, () -> client.post("<a/>", "/x"));
		assertEquals(0, received.size());
	}
}
