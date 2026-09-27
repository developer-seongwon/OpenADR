package com.avob.openadr.server.common.vtn.service.push;

import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.fasterxml.jackson.annotation.JsonCreator;

public class VenCommandDto<T> {

	private String venUsername;

	private String venPushUrl;

	private String venTransport;

	private boolean xmlSignature;

	private String payload;

	private Class<T> payloadClass;

	public VenCommandDto() {
	}

	/**
	 * Jackson 3 는 인자 있는 생성자를 크리에이터 후보로 더 적극적으로 집는다.
	 * 그대로 두면 역직렬화 때 이 생성자를 골라 ven 에 null 을 넣고 NPE 가 난다.
	 * 기본 생성자와 세터로 채우도록 이 생성자는 크리에이터에서 제외한다.
	 * Jackson 2 에서는 기본 생성자를 알아서 골랐다.
	 */
	@JsonCreator(mode = JsonCreator.Mode.DISABLED)
	public VenCommandDto(Ven ven, String payload, Class<T> klass) {
		this.setVenUsername(ven.getUsername());
		this.setVenPushUrl(ven.getPushUrl());
		this.setVenTransport(ven.getTransport());
		this.setXmlSignature(ven.getXmlSignature());
		this.setPayload(payload);
		this.setPayloadClass(klass);
	}

	public String getVenPushUrl() {
		return venPushUrl;
	}

	public void setVenPushUrl(String venPushUrl) {
		this.venPushUrl = venPushUrl;
	}

	public boolean isXmlSignature() {
		return xmlSignature;
	}

	public void setXmlSignature(boolean xmlSignature) {
		this.xmlSignature = xmlSignature;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getVenUsername() {
		return venUsername;
	}

	@Override
	public String toString() {
		return "VenCommandDto [venUsername=" + venUsername + ", venPushUrl=" + venPushUrl + ", xmlSignature="
				+ xmlSignature + ", payload=" + payload + "]";
	}

	public void setVenUsername(String venUsername) {
		this.venUsername = venUsername;
	}

	public Class<T> getPayloadClass() {
		return payloadClass;
	}

	public void setPayloadClass(Class<T> payloadClass) {
		this.payloadClass = payloadClass;
	}

	public String getVenTransport() {
		return venTransport;
	}

	public void setVenTransport(String venTransport) {
		this.venTransport = venTransport;
	}

}
