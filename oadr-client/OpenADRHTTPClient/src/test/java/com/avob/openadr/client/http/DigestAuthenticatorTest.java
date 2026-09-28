package com.avob.openadr.client.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Digest 헤더 계산을 RFC 2617 3.5 절의 예제로 확인한다. 이 값이 맞으면 서버 구현과 상관없이 계산식은 맞다
 */
public class DigestAuthenticatorTest {

	private static final String RFC_CHALLENGE = "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\", "
			+ "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

	@Test
	public void rfc2617Example() {
		DigestAuthenticator digest = new DigestAuthenticator("Mufasa", "Circle Of Life", "ignored", "ignored");
		assertTrue(digest.updateFromChallenge(RFC_CHALLENGE));

		String header = digest.authorization("GET", "/dir/index.html", "0a4f113b");
		Map<String, String> params = DigestAuthenticator.parseChallenge(header);

		// RFC 2617 에 적힌 기대값
		assertEquals("6629fae49393a05397450978507c4ef1", params.get("response"));
		assertEquals("testrealm@host.com", params.get("realm"));
		assertEquals("auth", params.get("qop"));
		assertEquals("00000001", params.get("nc"));
		assertEquals("0a4f113b", params.get("cnonce"));
		assertEquals("5ccc069c403ebaf9f0171e9517f40e41", params.get("opaque"));
		assertEquals("/dir/index.html", params.get("uri"));
	}

	@Test
	public void nonceCountIncreases() {
		DigestAuthenticator digest = new DigestAuthenticator("Mufasa", "Circle Of Life", "ignored", "ignored");
		digest.updateFromChallenge(RFC_CHALLENGE);
		digest.authorization("GET", "/", "a");
		Map<String, String> second = DigestAuthenticator.parseChallenge(digest.authorization("GET", "/", "b"));
		assertEquals("00000002", second.get("nc"));
	}

	@Test
	public void preemptiveWithoutQop() {
		// 챌린지를 받기 전 첫 요청은 qop 없는 RFC 2069 방식이다
		DigestAuthenticator digest = new DigestAuthenticator("ven1", "pw", "realm", "nonce");
		Map<String, String> params = DigestAuthenticator.parseChallenge(digest.authorization("POST", "/x"));
		assertNull(params.get("qop"));
		String ha1 = DigestAuthenticator.md5Hex("ven1:realm:pw");
		String ha2 = DigestAuthenticator.md5Hex("POST:/x");
		assertEquals(DigestAuthenticator.md5Hex(ha1 + ":nonce:" + ha2), params.get("response"));
	}

	@Test
	public void notADigestChallenge() {
		DigestAuthenticator digest = new DigestAuthenticator("u", "p", "r", "n");
		assertNull(DigestAuthenticator.parseChallenge("Basic realm=\"x\""));
		assertFalse(digest.updateFromChallenge("Basic realm=\"x\""));
		assertFalse(digest.updateFromChallenge("Digest realm=\"x\""));
	}
}
