package com.avob.openadr.client.xmpp.oadr20b;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jivesoftware.smack.xml.SmackXmlParser;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.stax.StaxXmlPullParserFactory;
import org.junit.jupiter.api.Test;

import com.ctc.wstx.stax.WstxInputFactory;

/**
 * smack 이 XMPP 스트림을 읽는 경로(SmackXmlParser)가 Woodstox 를 쓰는지,
 * 그리고 오래 붙어 있는 연결에서도 메시지를 계속 읽는지 확인한다.
 *
 * 배경: 자바 24 부터 JDK 기본 XML 제한 때문에 JDK 내장 StAX 로는 연결 하나에서 이스케이프된 본문이
 * 약 1MB 쌓이면 그 뒤 스탠자가 전부 버려졌다. Woodstox 를 클래스패스에 두어서 해결했다
 * (OpenADRXMPPClient pom 주석 참고).
 */
class SmackXmlParserTest {

	/** 이벤트가 많은 oadrDistributeEvent 처럼 기본 엔티티로 이스케이프된 OpenADR XML 을 흉내 낸다 */
	private static final String ESCAPED_CHUNK = "&lt;ei:eiEvent&gt;&lt;ei:eventID&gt;123&lt;/ei:eventID&gt;&lt;/ei:eiEvent&gt;";

	private static final String UNESCAPED_CHUNK = "<ei:eiEvent><ei:eventID>123</ei:eventID></ei:eiEvent>";

	/** 닫히지 않은 XMPP 스트림. 실제 연결처럼 스트림 하나에 메시지가 계속 이어진다 */
	private static String stream(int messages, int bodyChars) {
		StringBuilder body = new StringBuilder();
		while (body.length() < bodyChars) {
			body.append(ESCAPED_CHUNK);
		}
		StringBuilder sb = new StringBuilder(
				"<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
		for (int i = 0; i < messages; i++) {
			sb.append("<message id='m").append(i).append("'><body>").append(body).append("</body></message>");
		}
		return sb.toString();
	}

	/** smack 파서로 본문을 expected 개 읽는다. 각 본문이 이스케이프가 풀린 XML 인지 본다 */
	private static int readBodies(XmlPullParser parser, int expected) throws Exception {
		int count = 0;
		while (count < expected) {
			XmlPullParser.Event event = parser.next();
			if (event == XmlPullParser.Event.START_ELEMENT && "body".equals(parser.getName())) {
				String text = parser.nextText();
				assertTrue(text.startsWith(UNESCAPED_CHUNK), "body must be unescaped OpenADR xml");
				count++;
			}
			if (event == XmlPullParser.Event.END_DOCUMENT) {
				break;
			}
		}
		return count;
	}

	@Test
	void smackUsesItsStaxFactoryBackedByWoodstox() {
		// smack 기본 팩토리를 그대로 쓰고(다른 팩토리를 끼워 넣지 않는다), StAX 구현체만 Woodstox 로 잡혀야 한다
		assertInstanceOf(StaxXmlPullParserFactory.class, SmackXmlParser.getXmlPullParserFactory());
		assertInstanceOf(WstxInputFactory.class, XMLInputFactory.newInstance());
	}

	@Test
	void readsManySmallBodiesOnOneStream() throws Exception {
		// 이스케이프된 본문 약 1MB. JDK 내장 StAX 로는 여기서 깨졌다
		XmlPullParser parser = SmackXmlParser.newXmlParser(new StringReader(stream(200, 5_000)));
		assertEquals(200, readBodies(parser, 200));
	}

	@Test
	void readsManyLargeBodiesOnOneStream() throws Exception {
		// 이스케이프된 본문 약 4MB
		XmlPullParser parser = SmackXmlParser.newXmlParser(new StringReader(stream(20, 200_000)));
		assertEquals(20, readBodies(parser, 20));
	}

	@Test
	void declaredEntitiesAreNeverExpanded() {
		// smack 이 SUPPORT_DTD 를 끄므로 DTD 로 선언한 엔티티는 펼쳐지면 안 된다(엔티티 확장 공격 방지).
		// JDK StAX 는 DTD 를 만나면 예외를 던지고, Woodstox 는 DTD 를 건너뛰고 &a; 를 펼치지 않은 채 둔다.
		// 어느 쪽이든 "aaaa" 가 본문에 나오면 안 된다
		String withDtd = "<?xml version='1.0'?><!DOCTYPE stream [<!ENTITY a 'aaaa'>]>"
				+ "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'>"
				+ "<message><body>x&a;y</body></message>";
		String body;
		try {
			body = firstBody(SmackXmlParser.newXmlParser(new StringReader(withDtd)));
		} catch (Exception e) {
			// 파싱을 거절한 것도 통과
			return;
		}
		assertFalse(body != null && body.contains("aaaa"), "declared entity must not be expanded: " + body);
	}

	/** 첫 body 의 텍스트. 없으면 null */
	private static String firstBody(XmlPullParser parser) throws Exception {
		while (true) {
			XmlPullParser.Event event = parser.next();
			if (event == XmlPullParser.Event.START_ELEMENT && "body".equals(parser.getName())) {
				return parser.nextText();
			}
			if (event == XmlPullParser.Event.END_DOCUMENT) {
				return null;
			}
		}
	}

	@Test
	void jdkBuiltinStaxStillFailsOnLongStreamOnThisJdk() {
		// Woodstox 가 왜 필요한지 남겨 두는 재현 테스트. smack 과 같은 설정으로 JDK 내장 StAX 를 직접 쓴다.
		// JDK 기본 제한이 바뀌어서 여기가 깨지면(예외가 안 나면) Woodstox 없이도 되는지 다시 보면 된다
		XMLInputFactory jdk = XMLInputFactory.newDefaultFactory();
		jdk.setProperty(XMLInputFactory.IS_COALESCING, true);
		jdk.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		jdk.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		jdk.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		assertThrows(XMLStreamException.class, () -> {
			XMLStreamReader reader = jdk.createXMLStreamReader(new StringReader(stream(200, 5_000)));
			while (reader.hasNext()) {
				reader.next();
			}
		});
	}
}
