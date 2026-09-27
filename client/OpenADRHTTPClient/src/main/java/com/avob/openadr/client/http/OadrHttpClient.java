package com.avob.openadr.client.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * OpenADR 메시지를 POST 로 보내는 HTTP 클라이언트. 만드는 건 {@link OadrHttpClientBuilder} 가 한다.
 *
 * 예전에는 Apache httpclient 4 위에서 돌았다. 지금은 자바 표준 java.net.http.HttpClient 를 써서
 * 이 모듈은 외부 HTTP 라이브러리 없이 동작한다. 바꾸면서 Apache 가 알아서 해 주던 일을 여기서 한다.
 *
 * - Basic, Digest 인증 헤더: 기본 호스트(빌더의 withDefault*Authentication 에 준 주소)로 가는
 *   요청에만 붙인다. Apache 의 AuthScope 가 호스트와 포트로 범위를 제한하던 것과 같다
 * - Digest 401 재시도: {@link DigestAuthenticator} 참고
 * - http 차단: enableHttp 가 꺼져 있으면 http 주소로는 보내지 않는다.
 *   Apache 는 https 소켓 팩토리만 등록해서 http 요청이 실패했다
 * - 동시 요청 수: Apache 의 연결 풀(withPooling) 크기만큼만 동시에 보낸다. 자바 HttpClient 는
 *   연결 풀 크기를 클라이언트별로 정할 수 없어서 세마포어로 동시 요청 수를 묶는다
 */
public class OadrHttpClient {

	/**
	 * 요청 본문의 Content-Type. withHeader 로 Content-Type 을 주면 그 값을 쓴다.
	 *
	 * Apache 의 StringEntity 기본값은 text/plain; charset=ISO-8859-1 이었다.
	 * JAXB 가 만든 XML 은 UTF-8 이라고 선언돼 있어서, 라틴 문자 밖의 글자가 들어가면 깨질 수 있었다.
	 * 문자셋만 UTF-8 로 바꾼다
	 */
	static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=UTF-8";

	private final HttpClient client;
	private final URI defaultBaseUri;
	private final List<Map.Entry<String, String>> defaultHeaders;
	private final boolean enableHttp;
	private final Semaphore concurrency;

	/** 인증 헤더를 붙일 대상. withDefault*Authentication 에 준 주소다 */
	private final URI authTarget;
	private final String basicAuthorization;
	private final DigestAuthenticator digest;

	OadrHttpClient(HttpClient client, URI defaultBaseUri, List<Map.Entry<String, String>> defaultHeaders,
			boolean enableHttp, int maxConcurrentRequests, URI authTarget, String basicAuthorization,
			DigestAuthenticator digest) {
		this.client = client;
		this.defaultBaseUri = defaultBaseUri;
		this.defaultHeaders = List.copyOf(defaultHeaders);
		this.enableHttp = enableHttp;
		this.concurrency = new Semaphore(maxConcurrentRequests, true);
		this.authTarget = authTarget;
		this.basicAuthorization = basicAuthorization;
		this.digest = digest;
	}

	/**
	 * 기본 호스트의 경로로 보낸다. 주소는 기본 호스트 주소의 경로 뒤에 path 를 이어 붙여 만든다
	 */
	public HttpResponse<String> post(String body, String path) throws IOException, URISyntaxException {
		return post(body, null, path);
	}

	/**
	 * host 가 있으면 host + path 로, 없으면 기본 호스트로 보낸다.
	 * VTN 이 여러 VEN 에게 푸시할 때처럼 요청마다 대상이 다른 경우 host 를 준다
	 */
	public HttpResponse<String> post(String body, String host, String path) throws IOException, URISyntaxException {
		URI uri = (host != null) ? new URI(host + path) : resolveDefault(path);

		if (!enableHttp && "http".equalsIgnoreCase(uri.getScheme())) {
			throw new IOException("http 요청이 꺼져 있다(enableHttp=false): " + uri);
		}

		try {
			concurrency.acquire();
			try {
				HttpResponse<String> response = send(uri, body, preemptiveAuthorization(uri));
				if (response.statusCode() == 401 && digest != null && inAuthScope(uri)) {
					Optional<String> challenge = response.headers().allValues("WWW-Authenticate").stream()
							.filter(v -> DigestAuthenticator.parseChallenge(v) != null).findFirst();
					if (challenge.isPresent() && digest.updateFromChallenge(challenge.get())) {
						response = send(uri, body, digest.authorization("POST", requestUri(uri)));
					}
				}
				return response;
			} finally {
				concurrency.release();
			}
		} catch (InterruptedException e) {
			// 인터럽트 상태를 되살리고, 호출부가 이미 잡고 있는 IOException 계열로 넘긴다
			Thread.currentThread().interrupt();
			InterruptedIOException interrupted = new InterruptedIOException("요청 중 인터럽트: " + uri);
			interrupted.initCause(e);
			throw interrupted;
		}
	}

	private HttpResponse<String> send(URI uri, String body, String authorization)
			throws IOException, InterruptedException {
		HttpRequest.Builder request = HttpRequest.newBuilder(uri)
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
		boolean contentTypeGiven = false;
		for (Map.Entry<String, String> header : defaultHeaders) {
			request.header(header.getKey(), header.getValue());
			contentTypeGiven |= "Content-Type".equalsIgnoreCase(header.getKey());
		}
		if (!contentTypeGiven) {
			request.header("Content-Type", DEFAULT_CONTENT_TYPE);
		}
		if (authorization != null) {
			request.header("Authorization", authorization);
		}
		return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

	private String preemptiveAuthorization(URI uri) {
		if (!inAuthScope(uri)) {
			return null;
		}
		if (digest != null) {
			return digest.authorization("POST", requestUri(uri));
		}
		return basicAuthorization;
	}

	/**
	 * Apache 의 AuthScope(host, port) 와 같은 기준이다. 포트를 안 적은 주소로 만들었으면 포트는 따지지 않는다
	 */
	private boolean inAuthScope(URI uri) {
		if (authTarget == null || uri.getHost() == null || !uri.getHost().equalsIgnoreCase(authTarget.getHost())) {
			return false;
		}
		return authTarget.getPort() == -1 || authTarget.getPort() == effectivePort(uri);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() != -1) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	/** Digest 의 uri 항목. 요청 줄에 실리는 경로와 쿼리다 */
	static String requestUri(URI uri) {
		String path = uri.getRawPath();
		if (path == null || path.isEmpty()) {
			path = "/";
		}
		return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
	}

	/**
	 * 예전 new URIBuilder(defaultBaseUri).setPath(defaultBaseUri.getPath() + path) 와 같다.
	 * 기본 호스트의 스킴, 호스트, 포트는 그대로 두고 경로만 이어 붙인다
	 */
	private URI resolveDefault(String path) throws URISyntaxException {
		if (defaultBaseUri == null) {
			throw new URISyntaxException(String.valueOf(path), "기본 호스트가 없다. host 를 주거나 빌더에 withDefaultHost 를 줘야 한다");
		}
		String basePath = defaultBaseUri.getPath() == null ? "" : defaultBaseUri.getPath();
		return new URI(defaultBaseUri.getScheme(), defaultBaseUri.getUserInfo(), defaultBaseUri.getHost(),
				defaultBaseUri.getPort(), basePath + path, defaultBaseUri.getQuery(), defaultBaseUri.getFragment());
	}
}
