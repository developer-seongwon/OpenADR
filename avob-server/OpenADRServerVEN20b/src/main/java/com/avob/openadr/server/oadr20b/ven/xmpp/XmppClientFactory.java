package com.avob.openadr.server.oadr20b.ven.xmpp;

import com.avob.openadr.client.xmpp.oadr20b.OadrXmppClient20b;
import com.avob.openadr.client.xmpp.oadr20b.OadrXmppClient20bBuilder;
import com.avob.openadr.client.xmpp.oadr20b.OadrXmppException;

/**
 * XMPP 클라이언트를 실제로 만드는 지점.
 *
 * builder.build() 를 MultiVtnConfig 안에서 직접 부르면 그 순간 소켓이 열린다.
 * 그래서 "XMPP 설정이 실패하는 경우" 를 테스트하려면 진짜로 붙어보고 실패하기를
 * 기다리는 수밖에 없었고, 그러면 결과가 주변 상태에 좌우된다. 서버가 떠 있으면
 * 타임아웃을 기다리고, 어쩌다 연결이 성공하면 테스트가 깨진다.
 *
 * 생성을 이 인터페이스 뒤로 빼서 테스트가 갈아 끼울 수 있게 했다.
 * 운영에서는 DefaultXmppClientFactory 가 쓰인다.
 */
public interface XmppClientFactory {

	OadrXmppClient20b build(OadrXmppClient20bBuilder builder) throws OadrXmppException;

}
