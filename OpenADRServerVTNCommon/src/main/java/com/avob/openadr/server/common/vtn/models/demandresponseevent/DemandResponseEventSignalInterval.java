package com.avob.openadr.server.common.vtn.models.demandresponseevent;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DemandResponseEventSignalInterval {
	@Column(name = "interval_value")
	private Float value;
	@Column(name = "duration")
	private String duration;

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
}
