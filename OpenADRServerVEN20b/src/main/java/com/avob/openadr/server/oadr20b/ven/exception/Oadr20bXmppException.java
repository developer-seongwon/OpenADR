package com.avob.openadr.server.oadr20b.ven.exception;

/**
 * 아무 데서도 던지지도 잡지도 않는다.
 *
 * XMPP 쪽 오류는 smack 예외를 그대로 올리거나 Oadr20bException 으로 감싸고 있다. 지워도 된다.
 */
@Deprecated
public class Oadr20bXmppException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3432515375946333217L;

}
