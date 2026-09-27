package com.avob.openadr.server.common.vtn.models.demandresponseevent;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import com.avob.openadr.server.common.vtn.models.venmarketcontext.VenMarketContext;

@Embeddable
public class DemandResponseEventDescriptor {

	@ManyToOne
	@NotNull
	@JoinColumn(name = "market_context_id")
	private VenMarketContext marketContext;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "oadr_profile")
	private DemandResponseEventOadrProfileEnum oadrProfile;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "state")
	private DemandResponseEventStateEnum state = DemandResponseEventStateEnum.ACTIVE;

	@Column(name = "modification_number")
	private long modificationNumber = 0;

	@Column(name = "priority")
	private long priority = 0;

	@Column(name = "test_event")
	private boolean testEvent = false;

	@Column(name = "vtn_comment")
	private String vtnComment;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "response_required")
	private DemandResponseEventResponseRequiredEnum responseRequired;

	public VenMarketContext getMarketContext() {
		return marketContext;
	}

	public void setMarketContext(VenMarketContext marketContext) {
		this.marketContext = marketContext;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public boolean isTestEvent() {
		return testEvent;
	}

	public void setTestEvent(boolean testEvent) {
		this.testEvent = testEvent;
	}

	public String getVtnComment() {
		return vtnComment;
	}

	public void setVtnComment(String vtnComment) {
		this.vtnComment = vtnComment;
	}

	public DemandResponseEventResponseRequiredEnum getResponseRequired() {
		return responseRequired;
	}

	public void setResponseRequired(DemandResponseEventResponseRequiredEnum responseRequired) {
		this.responseRequired = responseRequired;
	}

	public DemandResponseEventOadrProfileEnum getOadrProfile() {
		return oadrProfile;
	}

	public void setOadrProfile(DemandResponseEventOadrProfileEnum oadrProfile) {
		this.oadrProfile = oadrProfile;
	}

	public long getModificationNumber() {
		return modificationNumber;
	}

	public void setModificationNumber(long modificationNumber) {
		this.modificationNumber = modificationNumber;
	}

	public DemandResponseEventStateEnum getState() {
		return state;
	}

	public void setState(DemandResponseEventStateEnum state) {
		this.state = state;
	}

}
