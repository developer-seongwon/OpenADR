package com.avob.openadr.server.common.vtn.models.ven;

import com.avob.openadr.server.common.vtn.models.user.AbstractUserDto;

public class VenDto extends AbstractUserDto {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6150349820747401714L;

	private String oadrName;

	private String oadrProfil;

	private String transport;

	private String pushUrl;

	private Boolean httpPullModel;

	private Long lastUpdateDatetime;

	private String registrationId;

	private Boolean reportOnly;

	private Boolean xmlSignature;

	/**
	 * VTN 이 만든 인증서 묶음이 남아 있어서 다시 내려받을 수 있는지(GET /Ven/{venID}/credentials).
	 * VEN 하나를 볼 때(findVenByUsername)만 채운다. 목록에서는 비어 있다
	 */
	private Boolean credentialsAvailable;

	public String getOadrName() {
		return oadrName;
	}

	public void setOadrName(String oadrName) {
		this.oadrName = oadrName;
	}

	public String getOadrProfil() {
		return oadrProfil;
	}

	public void setOadrProfil(String oadrProfil) {
		this.oadrProfil = oadrProfil;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public String getPushUrl() {
		return pushUrl;
	}

	public void setPushUrl(String pushUrl) {
		this.pushUrl = pushUrl;
	}

	public Boolean getHttpPullModel() {
		return httpPullModel;
	}

	public void setHttpPullModel(Boolean httpPullModel) {
		this.httpPullModel = httpPullModel;
	}

	public Long getLastUpdateDatetime() {
		return lastUpdateDatetime;
	}

	public void setLastUpdateDatetime(Long lastUpdateDatetime) {
		this.lastUpdateDatetime = lastUpdateDatetime;
	}

	public String getRegistrationId() {
		return registrationId;
	}

	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}

	public Boolean getReportOnly() {
		return reportOnly;
	}

	public void setReportOnly(Boolean reportOnly) {
		this.reportOnly = reportOnly;
	}

	public Boolean getXmlSignature() {
		return xmlSignature;
	}

	public void setXmlSignature(Boolean xmlSignature) {
		this.xmlSignature = xmlSignature;
	}

	public Boolean getCredentialsAvailable() {
		return credentialsAvailable;
	}

	public void setCredentialsAvailable(Boolean credentialsAvailable) {
		this.credentialsAvailable = credentialsAvailable;
	}
}
