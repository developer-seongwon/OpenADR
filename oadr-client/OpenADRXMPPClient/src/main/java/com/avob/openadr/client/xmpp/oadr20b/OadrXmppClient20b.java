package com.avob.openadr.client.xmpp.oadr20b;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.sasl.core.SASLAnonymous;
import org.jivesoftware.smack.sasl.javax.SASLExternalMechanism;
import org.jivesoftware.smack.sasl.javax.SASLPlainMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo.Feature;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.disco.packet.DiscoverItems.Item;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import com.avob.openadr.model.oadr20b.exception.Oadr20bMarshalException;

public class OadrXmppClient20b {

	public static final String OADR_NAMESPACE = "http://openadr.org/openadr2";

	public static final String OADR_SERVICES_NAMESPACE = OADR_NAMESPACE + "#services";

	public static final String OADR_EVENT_SERVICE_NAMESPACE = "http://openadr.org/OpenADR2/EiEvent";

	public static final String OADR_REPORT_SERVICE_NAMESPACE = "http://openadr.org/OpenADR2/EiReport";

	public static final String OADR_OPT_SERVICE_NAMESPACE = "http://openadr.org/OpenADR2/EiOpt";

	public static final String OADR_REGISTERPARTY_SERVICE_NAMESPACE = "http://openadr.org/OpenADR2/EiRegisterParty";

	public static final String XMPP_OADR_SUBDOMAIN = "xmpp";

	private XMPPTCPConnection connection;

	private DomainBareJid domainJid;

	private ChatManager chatManager;

	private Map<String, Jid> discoveredXmppOadrServices;

	/*
	 * TLS 재료는 SSLContext 가 아니라 KeyManagerFactory, TrustManagerFactory 로 받는다.
	 * smack 4.3 은 넘겨준 SSLContext 를 그대로 썼는데, 4.5 에는 setCustomSSLContext 가 없고
	 * 대신 쓰는 setSslContextFactory 는 받은 SSLContext 에 init() 을 다시 호출한다.
	 * 그러면 우리가 넣어 둔 클라이언트 인증서와 신뢰 인증서가 기본값으로 덮여서
	 * TLS 핸드셰이크가 깨진다. smack 이 권하는 방식대로 키 매니저와 트러스트 매니저를 넘긴다.
	 * 둘 다 null 이면 smack 기본값(JDK 기본 신뢰 저장소, 클라이언트 인증서 없음)을 쓴다.
	 */
	public static XMPPTCPConnectionConfiguration anonymousConnection(String host, int port, String domain,
			String resource, KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory)
			throws OadrXmppException {
		try {
			XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder().setHost(host)
					.setPort(port).performSaslAnonymousAuthentication().setResource(resource).setXmppDomain(domain);
			applyTls(builder, keyManagerFactory, trustManagerFactory);
			return builder.build();
		} catch (XmppStringprepException e) {
			throw new OadrXmppException(e);
		}
	}

	public static XMPPTCPConnectionConfiguration passwordConnection(String host, int port, String domain,
			String resource, KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory,
			String username, String password) throws OadrXmppException {
		try {
			XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder().setHost(host)
					.setPort(port).setUsernameAndPassword(username, password)
					.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).setCompressionEnabled(false)
					.setResource(resource).setXmppDomain(domain);
			applyTls(builder, keyManagerFactory, trustManagerFactory);
			return builder.build();
		} catch (XmppStringprepException e) {
			throw new OadrXmppException(e);
		}
	}

	private static void applyTls(XMPPTCPConnectionConfiguration.Builder builder, KeyManagerFactory keyManagerFactory,
			TrustManagerFactory trustManagerFactory) {
		if (keyManagerFactory != null) {
			builder.setKeyManagers(keyManagerFactory.getKeyManagers());
		}
		if (trustManagerFactory != null) {
			builder.setCustomX509TrustManager(x509TrustManager(trustManagerFactory));
		}
	}

	// TrustManagerFactory 는 배열을 주지만 smack 은 X509TrustManager 하나만 받는다.
	// PKIX 팩토리는 X509TrustManager 하나를 돌려준다
	private static X509TrustManager x509TrustManager(TrustManagerFactory trustManagerFactory) {
		for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
			if (trustManager instanceof X509TrustManager x509) {
				return x509;
			}
		}
		throw new IllegalArgumentException("TrustManagerFactory has no X509TrustManager");
	}

	public OadrXmppClient20b(String userJid, XMPPTCPConnection connection, String domain,
			StanzaListener onMessageListener) throws OadrXmppException {
		this.connection = connection;

		try {
			SASLAnonymous mechanism = new SASLAnonymous();
			SASLExternalMechanism ext = new SASLExternalMechanism();
			SASLPlainMechanism plain = new SASLPlainMechanism();
			SASLAuthentication.registerSASLMechanism(mechanism);
			SASLAuthentication.registerSASLMechanism(ext);
			SASLAuthentication.registerSASLMechanism(plain);
			SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
			SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
			setDomainJid(JidCreate.domainBareFrom(XMPP_OADR_SUBDOMAIN + "." + domain));

			chatManager = ChatManager.getInstanceFor(connection);

			if (onMessageListener != null) {
				this.connection.addAsyncStanzaListener(onMessageListener, StanzaTypeFilter.MESSAGE);
			}

			this.connection.connect().login(); // Establishes a connection to the server
			if (this.connection.isConnected() && this.connection.isAuthenticated()) {

				

				// smack 4.4 부터 스탠자는 new 가 아니라 연결의 StanzaFactory 로 만든다
				Presence p = this.connection.getStanzaFactory().buildPresenceStanza().ofType(Type.available).build();
				this.connection.sendStanza(p);

				boolean hasXmppOadrFeature = this.hasXmppOadrFeature();

				if (!hasXmppOadrFeature) {
					disconnectQuietly();
					throw new OadrXmppException("Xmpp Server does not provide OpenADR feature");
				}

				discoveredXmppOadrServices = this.discoverXmppOadrServices();
								

			} else {
				disconnectQuietly();
				throw new OadrXmppException("Connection refused by Xmpp server ");
			}

		} catch (XMPPException | SmackException | IOException e) {
			disconnectQuietly();
			throw new OadrXmppException(e);
		} catch (InterruptedException e) {
			disconnectQuietly();
			Thread.currentThread().interrupt();
			throw new OadrXmppException(e);
		}
		
	}

	/**
	 * 연결이나 로그인에 실패했을 때 반쯤 열린 연결을 닫는다. 호출하는 쪽(VEN)이 다시 시도할 때
	 * 이전 연결의 읽기, 쓰기 스레드가 남지 않게 한다
	 */
	private void disconnectQuietly() {
		try {
			if (this.connection.isConnected()) {
				this.connection.disconnect();
			}
		} catch (RuntimeException e) {
			// 닫다가 난 예외는 원래 실패 원인을 가리지 않도록 버린다
		}
	}

	public void sendMessage(Jid jid, String payload)
			throws Oadr20bMarshalException, XmppStringprepException, NotConnectedException, InterruptedException {
		EntityBareJid entityBareFrom = JidCreate.entityBareFrom(jid);
		Chat chatWith = chatManager.chatWith(entityBareFrom);
		Message message = this.connection.getStanzaFactory().buildMessageStanza().from(this.connection.getUser())
				.to(jid).setBody(payload).build();
		chatWith.send(message);
	}

	public boolean hasXmppOadrFeature()
			throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
		DiscoverInfo discoverInfo;
		discoverInfo = discoManager.discoverInfo(getDomainJid(), null);
		Iterator<Feature> it = discoverInfo.getFeatures().iterator();
		while (it.hasNext()) {
			DiscoverInfo.Feature identity = (DiscoverInfo.Feature) it.next();
			if (OADR_NAMESPACE.equals(identity.getVar())) {
				return true;
			}
		}
		return false;

	}

	public Map<String, Jid> discoverXmppOadrServices()
			throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
		Map<String, Jid> discoveredServiceJid = new HashMap<>();
		DiscoverItems discoverItems = discoManager.discoverItems(getDomainJid(), OADR_SERVICES_NAMESPACE);

		for (Item item : discoverItems.getItems()) {
			discoveredServiceJid.put(item.getNode(), item.getEntityID());
		}

		return discoveredServiceJid;
	}

	public Map<String, Jid> getDiscoveredXmppOadrServices() {
		return discoveredXmppOadrServices;
	}

	public String getClientJid() {
		return this.connection.getUser().asEntityFullJidIfPossible().toString();
	}
	
	public String getBareClientJid() {
		return this.connection.getUser().asBareJid().asUnescapedString();
	}


	public DomainBareJid getDomainJid() {
		return domainJid;
	}

	private void setDomainJid(DomainBareJid domainJid) {
		this.domainJid = domainJid;
	}

}
