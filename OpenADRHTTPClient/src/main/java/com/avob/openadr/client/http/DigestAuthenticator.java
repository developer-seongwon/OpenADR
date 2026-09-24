package com.avob.openadr.client.http;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP Digest 인증(RFC 2617) 헤더를 만든다.
 *
 * 자바 표준 HttpClient 에는 Digest 인증이 없다(Basic 도 챌린지를 받아야만 붙인다).
 * 예전 Apache httpclient 4 의 DigestScheme 이 하던 일 중 이 프로젝트가 쓰는 부분만 옮겼다.
 *
 * 동작 순서
 * 1. 빌더가 준 realm, nonce 로 첫 요청부터 헤더를 붙인다(선제 인증). 이때는 qop 가 없는
 *    RFC 2069 방식이다. OpenADR 2.0a 가 nonce 를 미리 합의해 두는 방식이라 가능하다.
 * 2. 서버가 401 과 함께 WWW-Authenticate: Digest ... 를 주면 그 realm, nonce, qop 로 바꿔서
 *    한 번 다시 보낸다. 미리 만든 nonce 가 만료됐거나(5분) 서버 키와 다를 때 이 경로를 탄다.
 *    VEN20b 는 key 를 "" 로 넘겨서 첫 nonce 가 항상 거절되므로 이 재시도가 없으면 인증이 안 된다.
 * 3. 그 뒤로는 서버가 준 nonce 로 계속 선제 인증하고, qop=auth 면 nc 를 1씩 올린다.
 *
 * 여러 스레드가 같은 클라이언트를 쓸 수 있어서 상태를 바꾸는 메서드는 synchronized 다.
 */
final class DigestAuthenticator {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final String username;
	private final String password;

	private String realm;
	private String nonce;
	private String opaque;
	/** null 이면 qop 없는 RFC 2069 방식, "auth" 면 RFC 2617 방식 */
	private String qop;
	private int nonceCount;

	DigestAuthenticator(String username, String password, String realm, String nonce) {
		this.username = username;
		this.password = password;
		this.realm = realm;
		this.nonce = nonce;
	}

	/**
	 * Authorization 헤더 값을 만든다.
	 *
	 * @param method HTTP 메서드(POST)
	 * @param requestUri 요청 줄에 들어가는 경로와 쿼리. 서버가 자기가 받은 URI 와 비교한다
	 */
	synchronized String authorization(String method, String requestUri) {
		return authorization(method, requestUri, HexFormat.of().formatHex(randomBytes(8)));
	}

	/**
	 * cnonce 를 밖에서 받는 버전. RFC 2617 에 실린 예제 값으로 계산이 맞는지 테스트하려고 연다
	 */
	synchronized String authorization(String method, String requestUri, String cnonce) {
		String ha1 = md5Hex(username + ":" + realm + ":" + password);
		String ha2 = md5Hex(method + ":" + requestUri);

		StringBuilder header = new StringBuilder("Digest ");
		header.append("username=\"").append(username).append("\", ");
		header.append("realm=\"").append(realm).append("\", ");
		header.append("nonce=\"").append(nonce).append("\", ");
		header.append("uri=\"").append(requestUri).append("\", ");

		String response;
		if (qop == null) {
			response = md5Hex(ha1 + ":" + nonce + ":" + ha2);
			header.append("response=\"").append(response).append("\"");
		} else {
			nonceCount++;
			String nc = String.format("%08x", nonceCount);
			response = md5Hex(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);
			header.append("response=\"").append(response).append("\", ");
			header.append("qop=").append(qop).append(", ");
			header.append("nc=").append(nc).append(", ");
			header.append("cnonce=\"").append(cnonce).append("\"");
		}
		// Apache DigestScheme 도 algorithm 을 따옴표 없이 붙였다
		header.append(", algorithm=MD5");
		if (opaque != null) {
			header.append(", opaque=\"").append(opaque).append("\"");
		}
		return header.toString();
	}

	/**
	 * 서버의 WWW-Authenticate 챌린지로 realm, nonce, qop 를 바꾼다.
	 *
	 * @return nonce 가 들어 있는 Digest 챌린지였으면 true. 이때만 재시도할 가치가 있다
	 */
	synchronized boolean updateFromChallenge(String wwwAuthenticate) {
		Map<String, String> params = parseChallenge(wwwAuthenticate);
		if (params == null || !params.containsKey("nonce")) {
			return false;
		}
		this.nonce = params.get("nonce");
		if (params.containsKey("realm")) {
			this.realm = params.get("realm");
		}
		this.opaque = params.get("opaque");
		// qop 는 "auth" 나 "auth,auth-int" 로 온다. 본문 해시가 필요한 auth-int 는 쓰지 않는다
		String challengeQop = params.get("qop");
		this.qop = null;
		if (challengeQop != null) {
			for (String token : challengeQop.split(",")) {
				if ("auth".equals(token.trim())) {
					this.qop = "auth";
				}
			}
		}
		this.nonceCount = 0;
		return true;
	}

	/**
	 * Digest a="b", c=d, e="f,g" 모양을 키, 값으로 나눈다. 따옴표 안의 쉼표는 값의 일부다.
	 * Digest 챌린지가 아니면 null.
	 */
	static Map<String, String> parseChallenge(String header) {
		if (header == null) {
			return null;
		}
		String trimmed = header.trim();
		if (trimmed.length() < 6 || !trimmed.regionMatches(true, 0, "Digest", 0, 6)) {
			return null;
		}
		Map<String, String> params = new LinkedHashMap<>();
		String rest = trimmed.substring(6);
		int i = 0;
		while (i < rest.length()) {
			while (i < rest.length() && (rest.charAt(i) == ',' || Character.isWhitespace(rest.charAt(i)))) {
				i++;
			}
			int eq = rest.indexOf('=', i);
			if (eq < 0) {
				break;
			}
			String key = rest.substring(i, eq).trim().toLowerCase();
			i = eq + 1;
			String value;
			if (i < rest.length() && rest.charAt(i) == '"') {
				StringBuilder sb = new StringBuilder();
				i++;
				while (i < rest.length() && rest.charAt(i) != '"') {
					if (rest.charAt(i) == '\\' && i + 1 < rest.length()) {
						i++;
					}
					sb.append(rest.charAt(i));
					i++;
				}
				i++;
				value = sb.toString();
			} else {
				int comma = rest.indexOf(',', i);
				int end = comma < 0 ? rest.length() : comma;
				value = rest.substring(i, end).trim();
				i = end;
			}
			params.put(key, value);
		}
		return params;
	}

	static String md5Hex(String value) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			return HexFormat.of().formatHex(md5.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// MD5 는 모든 자바 구현이 반드시 제공해야 하는 알고리즘이다
			throw new IllegalStateException(e);
		}
	}

	private static byte[] randomBytes(int size) {
		byte[] bytes = new byte[size];
		RANDOM.nextBytes(bytes);
		return bytes;
	}
}
