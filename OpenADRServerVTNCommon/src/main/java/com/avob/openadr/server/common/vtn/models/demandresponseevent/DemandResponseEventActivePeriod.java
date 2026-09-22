package com.avob.openadr.server.common.vtn.models.demandresponseevent;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class DemandResponseEventActivePeriod {

	@NotNull
	@Column(name = "active_period_start")
	private Long start;

	@Column(name = "active_period_end")
	private Long end;

	@NotNull
	@Column(name = "start_notification")
	private Long startNotification;

	/**
	 * Event active state duration as xml duration
	 */
	@NotNull
	@Column(name = "duration")
	private String duration;

	/**
	 * Event notification duration as xml duration
	 */
	@NotNull
	@Column(name = "notification_duration")
	private String notificationDuration;

	/**
	 * Event tolerance as xml duration
	 */
	@Column(name = "tolerance_duration")
	private String toleranceDuration;

	/**
	 * Event ramp up duration as xml duration
	 */
	@Column(name = "ramp_up_duration")
	private String rampUpDuration;

	/**
	 * Event recovery duration as xml duration
	 */
	@Column(name = "recovery_duration")
	private String recoveryDuration;

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getToleranceDuration() {
		return toleranceDuration;
	}

	public void setToleranceDuration(String toleranceDuration) {
		this.toleranceDuration = toleranceDuration;
	}

	public String getRampUpDuration() {
		return rampUpDuration;
	}

	public void setRampUpDuration(String rampUpDuration) {
		this.rampUpDuration = rampUpDuration;
	}

	public String getRecoveryDuration() {
		return recoveryDuration;
	}

	public void setRecoveryDuration(String recoveryDuration) {
		this.recoveryDuration = recoveryDuration;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public String getNotificationDuration() {
		return notificationDuration;
	}

	public void setNotificationDuration(String notificationDuration) {
		this.notificationDuration = notificationDuration;
	}

	public Long getStartNotification() {
		return startNotification;
	}

	public void setStartNotification(Long startNotification) {
		this.startNotification = startNotification;
	}

}
