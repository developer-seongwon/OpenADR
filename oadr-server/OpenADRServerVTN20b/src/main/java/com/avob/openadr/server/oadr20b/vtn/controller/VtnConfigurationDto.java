package com.avob.openadr.server.oadr20b.vtn.controller;

public class VtnConfigurationDto {
	/**
	 * VTN 이 응답(oadrCreatedPartyRegistration 등)에 싣는 vtnID. 설정 oadr.vtnid.
	 * 예전에는 여기에 VTN 서버 인증서 지문을 넣어서 화면의 VTN ID 가 실제 vtnID 와 달랐다
	 */
	private String vtnId;

	/**
	 * VTN 서버 인증서의 OpenADR 2.0b 지문. VTN ID 가 아니다. 인증서를 다시 만들면 바뀐다
	 */
	private String fingerprint;

	private Boolean supportPush;

	private Boolean supportUnsecuredHttpPush;

	private Long pullFrequencySeconds;

	private int port;

	private String contextPath;

	private String host;

	private String oadrVersion = "OpenADR 20b";

	private boolean supportCertificateGeneration = false;

	private boolean xsdValidation = false;

	private Long xmlSignatureReplayProtectSecond;

	private boolean saveVenDate = false;

	public String getVtnId() {
		return vtnId;
	}

	public Boolean getSupportPush() {
		return supportPush;
	}

	public Boolean getSupportUnsecuredHttpPush() {
		return supportUnsecuredHttpPush;
	}

	public Long getPullFrequencySeconds() {
		return pullFrequencySeconds;
	}

	public int getPort() {
		return port;
	}

	public String getContextPath() {
		return contextPath;
	}

	public String getHost() {
		return host;
	}

	public String getOadrVersion() {
		return oadrVersion;
	}

	public boolean isSupportCertificateGeneration() {
		return supportCertificateGeneration;
	}

	public void setSupportCertificateGeneration(boolean supportCertificateGeneration) {
		this.supportCertificateGeneration = supportCertificateGeneration;
	}

	public boolean isXsdValidation() {
		return xsdValidation;
	}

	public void setXsdValidation(boolean xsdValidation) {
		this.xsdValidation = xsdValidation;
	}

	public Long getXmlSignatureReplayProtectSecond() {
		return xmlSignatureReplayProtectSecond;
	}

	public void setXmlSignatureReplayProtectSecond(Long xmlSignatureReplayProtectSecond) {
		this.xmlSignatureReplayProtectSecond = xmlSignatureReplayProtectSecond;
	}

	public boolean isSaveVenDate() {
		return saveVenDate;
	}

	public void setVtnId(String vtnId) {
		this.vtnId = vtnId;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public void setSupportPush(Boolean supportPush) {
		this.supportPush = supportPush;
	}

	public void setSupportUnsecuredHttpPush(Boolean supportUnsecuredHttpPush) {
		this.supportUnsecuredHttpPush = supportUnsecuredHttpPush;
	}

	public void setPullFrequencySeconds(Long pullFrequencySeconds) {
		this.pullFrequencySeconds = pullFrequencySeconds;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setOadrVersion(String oadrVersion) {
		this.oadrVersion = oadrVersion;
	}

	public void setSaveVenDate(boolean saveVenDate) {
		this.saveVenDate = saveVenDate;
	}
}
