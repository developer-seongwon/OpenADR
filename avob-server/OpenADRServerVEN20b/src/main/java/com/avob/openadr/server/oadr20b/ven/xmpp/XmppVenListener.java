package com.avob.openadr.server.oadr20b.ven.xmpp;

import jakarta.annotation.Resource;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Localpart;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.oadr20b.ven.service.Oadr20bVENPayloadService;

@Service
public class XmppVenListener implements StanzaListener {

	// @Lazy: 순환 참조를 끊는다. MultiVtnConfig 가 XMPP 클라이언트를 만들 때 이 리스너를 넘기고,
	// 페이로드 서비스는 다시 MultiVtnConfig 를 쓴다(MultiVtnConfig -> 이 리스너 -> 페이로드 서비스 -> MultiVtnConfig).
	// 예전에는 spring.main.allow-circular-references=true 로 덮어 두었다.
	// 여기에는 프록시가 들어가고 진짜 서비스는 첫 메시지를 받을 때 찾는다.
	// 메시지는 MultiVtnConfig 초기화(XMPP 연결)가 끝난 뒤에야 의미가 있으니 늦게 찾아도 된다
	@Lazy
	@Resource
	private Oadr20bVENPayloadService oadr20bVENPayloadService;

	@Override
	public void processStanza(Stanza packet) throws NotConnectedException, InterruptedException, NotLoggedInException {

		Message message = (Message) packet;

		Jid from = packet.getFrom();

		Jid to = packet.getTo();

		Localpart localpartOrThrow = from.getLocalpartOrThrow();

		String vtnId = localpartOrThrow.asUnescapedString().toLowerCase();

		String payload = message.getBody();

		oadr20bVENPayloadService.xmppRequest(vtnId, to.asUnescapedString(), payload);

	}

}
