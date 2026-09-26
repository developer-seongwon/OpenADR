package com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.validator;

import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.springframework.validation.Errors;

import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventContentDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.embedded.DemandResponseEventSignalDto;

public class DemandResponseEventDtoValidator {

	private static final int SIGNAL_ID_MAX_LENGTH = 255;

	private DatatypeFactory datatypeFactory = null;

	public DemandResponseEventDtoValidator() throws DatatypeConfigurationException {
		datatypeFactory = DatatypeFactory.newInstance();
	}

	protected void failOnInvalidXmlDuration(String duration, Errors errors, String field) {
		if (duration != null) {
			try {
				datatypeFactory.newDuration(duration);
			} catch (Exception e) {

				errors.rejectValue(field, "field." + field + ".invalid", "Invalid XML duration format");
			}
		}
	}

	protected void failOnMissingOrEmpty(Object obj, Errors errors, String field) {
		if (obj == null || (obj instanceof String && "".equals(((String) obj).trim()))) {
			errors.rejectValue(field, "field.required", "Missing mandatory field");
		}
	}

	protected void failOnPresent(Object obj, Errors errors, String field) {
		if (obj != null) {
			errors.rejectValue(field, "field.must_not_be_set", "Field must not be set");
		}
	}

	protected void validateContent(DemandResponseEventContentDto dto, Errors errors) {

		if (dto.getSignals().isEmpty()) {
			errors.rejectValue("signals", "field.required", "At least one signal must be configured");
		}
		validateSignalIds(dto, errors);
	}

	/**
	 * signalID 는 자유 문자열이지만 한 이벤트 안에서는 겹치면 안 된다. VEN 이 시그널을 이 값으로 구분한다.
	 * 비워 둔 시그널은 순번(0, 1, 2 ...)을 쓰므로 직접 넣은 값이 순번 숫자와 겹쳐도 거절한다.
	 * 길이는 DB 컬럼(varchar 255)에 맞춘다
	 */
	private void validateSignalIds(DemandResponseEventContentDto dto, Errors errors) {
		Set<String> seen = new HashSet<>();
		for (int i = 0; i < dto.getSignals().size(); i++) {
			DemandResponseEventSignalDto signal = dto.getSignals().get(i);
			if (signal == null) {
				// 빈 시그널 자체는 여기서 다루지 않는다. 순번 자리만 잡아 둔다(null 원소에는 필드 경로를 걸 수 없다)
				seen.add(String.valueOf(i));
				continue;
			}
			String signalId = signal.getSignalId() == null ? "" : signal.getSignalId().trim();
			String effective = signalId.isEmpty() ? String.valueOf(i) : signalId;
			String field = "signals[" + i + "].signalId";
			if (signalId.length() > SIGNAL_ID_MAX_LENGTH) {
				errors.rejectValue(field, "field.signalId.tooLong",
						"signalId MUST be at most " + SIGNAL_ID_MAX_LENGTH + " characters");
			}
			if (!seen.add(effective)) {
				errors.rejectValue(field, "field.signalId.duplicate",
						"signalId MUST be unique in an event: " + effective);
			}
		}
	}

	protected void validateDescriptor(DemandResponseEventDto dto, Errors errors) {
		failOnMissingOrEmpty(dto.getDescriptor().getMarketContext(), errors, "descriptor.marketContext");
		failOnMissingOrEmpty(dto.getDescriptor().getOadrProfile(), errors, "descriptor.oadrProfile");
		failOnMissingOrEmpty(dto.getDescriptor().getResponseRequired(), errors, "descriptor.responseRequired");
		failOnPresent(dto.getDescriptor().getModificationNumber(), errors, "descriptor.modificationNumber");
		if (dto.getDescriptor().getPriority() != null && dto.getDescriptor().getPriority() < 0) {
			errors.rejectValue("descriptor.priority", "field.priority.invalid", "Priority MUST be greater than 0");
		}

	}

	protected void validateActivePeriod(DemandResponseEventDto dto, Errors errors) {

		failOnMissingOrEmpty(dto.getActivePeriod().getStart(), errors, "activePeriod.start");
		failOnMissingOrEmpty(dto.getActivePeriod().getDuration(), errors, "activePeriod.duration");
		failOnMissingOrEmpty(dto.getActivePeriod().getNotificationDuration(), errors,
				"activePeriod.notificationDuration");
		failOnMissingOrEmpty(dto.getActivePeriod().getToleranceDuration(), errors, "activePeriod.toleranceDuration");
		if (!"0".equals(dto.getActivePeriod().getDuration())) {
			failOnInvalidXmlDuration(dto.getActivePeriod().getDuration(), errors, "activePeriod.duration");
		}
		failOnInvalidXmlDuration(dto.getActivePeriod().getNotificationDuration(), errors,
				"activePeriod.notificationDuration");
		failOnInvalidXmlDuration(dto.getActivePeriod().getToleranceDuration(), errors,
				"activePeriod.toleranceDuration");
		failOnInvalidXmlDuration(dto.getActivePeriod().getRampUpDuration(), errors, "activePeriod.rampUpDuration");
		failOnInvalidXmlDuration(dto.getActivePeriod().getRecoveryDuration(), errors, "activePeriod.recoveryDuration");

	}

}
