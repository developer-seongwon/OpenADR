package com.avob.openadr.server.oadr20b.ven.xmpp;

import org.springframework.stereotype.Component;

import com.avob.openadr.client.xmpp.oadr20b.OadrXmppClient20b;
import com.avob.openadr.client.xmpp.oadr20b.OadrXmppClient20bBuilder;
import com.avob.openadr.client.xmpp.oadr20b.OadrXmppException;

/**
 * 운영에서 쓰는 구현. 빌더에게 그대로 맡긴다.
 */
@Component
public class DefaultXmppClientFactory implements XmppClientFactory {

	@Override
	public OadrXmppClient20b build(OadrXmppClient20bBuilder builder) throws OadrXmppException {
		return builder.build();
	}

}
