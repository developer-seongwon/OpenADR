package com.avob.openadr.model.oadr20a;

public class Oadr20aSecurity {

    // TLSv1 과 TLSv1.1 은 Java 17 의 jdk.tls.disabledAlgorithms 에 들어 있어 실제로는 안 쓰인다.
    // OpenADR 2.0a 규격이 요구하는 값이라 목록에는 남겨 둔다. 구형 피어를 만나면 의미가 있다.
    private static final String[] PROTOCOLS = new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" };

    // 규격은 아래 두 개를 요구한다. 그런데 Java 17 은 TLS_RSA_* 를 통째로 막아 놔서
    // TLS_RSA_WITH_AES_128_CBC_SHA 가 SSL 엔진에 아예 없다.
    // 그러면 서버 인증서가 RSA 일 때 쓸 수 있는 스위트가 하나도 안 남아 핸드셰이크가 거절된다.
    // 같은 AES-128-CBC-SHA 인데 키 교환만 ECDHE 인 스위트를 하나 더해 둔다.
    // 목록의 앞뒤 순서는 상관없다. 양쪽 모두 이 목록에서 교집합을 찾는다.
    private static final String[] CIPHERS = new String[] { "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA" };

    private Oadr20aSecurity() {
    }

    public static String[] getProtocols() {
        return PROTOCOLS;
    }

    public static String[] getCiphers() {
        return CIPHERS;
    }

}
