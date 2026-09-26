package com.avob.openadr.server.common.vtn.service.dtomapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.models.ItemBase;
import com.avob.openadr.server.common.vtn.models.ItemBaseDto;
import com.avob.openadr.server.common.vtn.models.Target;
import com.avob.openadr.server.common.vtn.models.TargetDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEvent;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEventActivePeriod;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEventBaseline;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEventDescriptor;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEventSignal;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEventSignalInterval;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventReadDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventUpdateDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.embedded.DemandResponseEventActivePeriodDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.embedded.DemandResponseEventBaselineDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.embedded.DemandResponseEventDescriptorDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.embedded.DemandResponseEventSignalDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.embedded.DemandResponseEventSignalIntervalDto;
import com.avob.openadr.server.common.vtn.service.VenMarketContextService;

/**
 * DR 이벤트와 그 안에 중첩된 값 객체들을 옮긴다. dozer 를 걷어내고 손으로 쓴 매퍼다.
 *
 * 이 계열은 그래프가 깊다.
 * event 아래에 descriptor, activePeriod, baseline, signal 이 있고
 * baseline 과 signal 아래에 다시 interval, itemBase, target 이 달린다.
 * dozer 가 이 전부를 이름으로 재귀 복사하고 있었다.
 *
 * 양방향으로 옮기면서 챙겨야 하는 게 세 가지다.
 *
 * 첫째, descriptor 의 marketContext 는 엔티티에서는 VenMarketContext 인데
 * DTO 에서는 이름 문자열이다. 들어올 때는 이름으로 DB 를 조회해야 한다.
 * 예전 MarketContextMapper 가 하던 일이고, 이 매퍼에서 유일하게 DB 를 건드리는 지점이다.
 *
 * 둘째, 엔티티 쪽 숫자와 불리언이 원시 타입이고 DTO 쪽은 박싱 타입이라
 * 역방향에서 null 이 들어오면 NPE 가 난다. 해당 필드는 null 검사를 거친다.
 *
 * 셋째, signals 는 엔티티에서 Set 이고 DTO 에서는 List 다.
 * 순서가 응답에 드러나는 자리라 LinkedHashSet 으로 받아 입력 순서를 지킨다.
 */
@Service
public class DemandResponseEventDtoMapper {

	@Resource
	private VenMarketContextService venMarketContextService;

	public DemandResponseEventReadDto toReadDto(DemandResponseEvent src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventReadDto dst = new DemandResponseEventReadDto();
		dst.setId(src.getId());
		dst.setCreatedTimestamp(src.getCreatedTimestamp());
		dst.setLastUpdateTimestamp(src.getLastUpdateTimestamp());
		dst.setPublished(src.isPublished());
		dst.setDescriptor(toDescriptorDto(src.getDescriptor()));
		dst.setActivePeriod(toActivePeriodDto(src.getActivePeriod()));
		dst.setBaseline(toBaselineDto(src.getBaseline()));
		dst.setSignals(toSignalDtoList(src.getSignals()));
		dst.setTargets(toTargetDtoList(src.getTargets()));
		return dst;
	}

	/**
	 * 생성 요청을 엔티티로 옮긴다. DemandResponseEventCreateDto 가 이 타입을 상속한다.
	 * id, createdTimestamp, lastUpdateTimestamp 는 서비스 계층이 직접 채우므로 건드리지 않는다.
	 */
	public DemandResponseEvent toEntity(DemandResponseEventDto src) {
		if (src == null) {
			return null;
		}
		DemandResponseEvent dst = new DemandResponseEvent();
		dst.setPublished(src.isPublished());
		dst.setDescriptor(toDescriptor(src.getDescriptor()));
		dst.setActivePeriod(toActivePeriod(src.getActivePeriod()));
		dst.setBaseline(toBaseline(src.getBaseline()));
		dst.setSignals(toSignalSet(src.getSignals()));
		dst.setTargets(toTargetList(src.getTargets()));
		return dst;
	}

	/**
	 * 수정 요청을 엔티티로 옮긴다.
	 * DemandResponseEventUpdateDto 에는 descriptor, activePeriod, baseline 이 없다.
	 * 호출부도 signals, targets, published 만 꺼내 쓰고 나머지는 기존 이벤트 것을 유지한다.
	 */
	public DemandResponseEvent toEntity(DemandResponseEventUpdateDto src) {
		if (src == null) {
			return null;
		}
		DemandResponseEvent dst = new DemandResponseEvent();
		dst.setPublished(src.getPublished() != null && src.getPublished());
		dst.setSignals(toSignalSet(src.getSignals()));
		dst.setTargets(toTargetList(src.getTargets()));
		return dst;
	}

	private DemandResponseEventDescriptorDto toDescriptorDto(DemandResponseEventDescriptor src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventDescriptorDto dst = new DemandResponseEventDescriptorDto();
		dst.setMarketContext(src.getMarketContext() == null ? null : src.getMarketContext().getName());
		dst.setOadrProfile(src.getOadrProfile());
		dst.setState(src.getState());
		dst.setModificationNumber(src.getModificationNumber());
		dst.setPriority(src.getPriority());
		dst.setTestEvent(src.isTestEvent());
		dst.setVtnComment(src.getVtnComment());
		dst.setResponseRequired(src.getResponseRequired());
		return dst;
	}

	private DemandResponseEventDescriptor toDescriptor(DemandResponseEventDescriptorDto src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventDescriptor dst = new DemandResponseEventDescriptor();
		if (src.getMarketContext() != null) {
			dst.setMarketContext(venMarketContextService.findOneByName(src.getMarketContext()));
		}
		dst.setOadrProfile(src.getOadrProfile());
		if (src.getState() != null) {
			dst.setState(src.getState());
		}
		if (src.getModificationNumber() != null) {
			dst.setModificationNumber(src.getModificationNumber());
		}
		if (src.getPriority() != null) {
			dst.setPriority(src.getPriority());
		}
		if (src.getTestEvent() != null) {
			dst.setTestEvent(src.getTestEvent());
		}
		dst.setVtnComment(src.getVtnComment());
		dst.setResponseRequired(src.getResponseRequired());
		return dst;
	}

	/**
	 * 엔티티에는 end 와 startNotification 이 더 있는데 DTO 에 대응하는 필드가 없다.
	 * 서비스 계층이 duration 으로 계산해 채우는 값이라 매핑 대상이 아니다.
	 */
	private DemandResponseEventActivePeriodDto toActivePeriodDto(DemandResponseEventActivePeriod src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventActivePeriodDto dst = new DemandResponseEventActivePeriodDto();
		dst.setStart(src.getStart());
		dst.setDuration(src.getDuration());
		dst.setNotificationDuration(src.getNotificationDuration());
		dst.setToleranceDuration(src.getToleranceDuration());
		dst.setRampUpDuration(src.getRampUpDuration());
		dst.setRecoveryDuration(src.getRecoveryDuration());
		return dst;
	}

	private DemandResponseEventActivePeriod toActivePeriod(DemandResponseEventActivePeriodDto src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventActivePeriod dst = new DemandResponseEventActivePeriod();
		dst.setStart(src.getStart());
		dst.setDuration(src.getDuration());
		dst.setNotificationDuration(src.getNotificationDuration());
		dst.setToleranceDuration(src.getToleranceDuration());
		dst.setRampUpDuration(src.getRampUpDuration());
		dst.setRecoveryDuration(src.getRecoveryDuration());
		return dst;
	}

	private DemandResponseEventBaselineDto toBaselineDto(DemandResponseEventBaseline src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventBaselineDto dst = new DemandResponseEventBaselineDto();
		dst.setStart(src.getStart());
		dst.setDuration(src.getDuration());
		dst.setBaselineId(src.getBaselineId());
		dst.setBaselineName(src.getBaselineName());
		dst.setResourceId(src.getResourceId());
		dst.setItemBase(toItemBaseDto(src.getItemBase()));
		dst.setIntervals(toIntervalDtoList(src.getIntervals()));
		return dst;
	}

	private DemandResponseEventBaseline toBaseline(DemandResponseEventBaselineDto src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventBaseline dst = new DemandResponseEventBaseline();
		dst.setStart(src.getStart());
		dst.setDuration(src.getDuration());
		dst.setBaselineId(src.getBaselineId());
		dst.setBaselineName(src.getBaselineName());
		dst.setResourceId(src.getResourceId());
		dst.setItemBase(toItemBase(src.getItemBase()));
		dst.setIntervals(toIntervalList(src.getIntervals()));
		return dst;
	}

	private List<DemandResponseEventSignalDto> toSignalDtoList(Set<DemandResponseEventSignal> src) {
		if (src == null) {
			return null;
		}
		List<DemandResponseEventSignalDto> dst = new ArrayList<>();
		for (DemandResponseEventSignal signal : src) {
			dst.add(toSignalDto(signal));
		}
		return dst;
	}

	private DemandResponseEventSignalDto toSignalDto(DemandResponseEventSignal src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventSignalDto dst = new DemandResponseEventSignalDto();
		dst.setSignalId(src.getSignalId());
		dst.setSignalName(src.getSignalName());
		dst.setSignalType(src.getSignalType());
		dst.setCurrentValue(src.getCurrentValue());
		dst.setItemBase(toItemBaseDto(src.getItemBase()));
		dst.setIntervals(toIntervalDtoList(src.getIntervals()));
		dst.setTargets(toTargetDtoList(src.getTargets()));
		return dst;
	}

	private Set<DemandResponseEventSignal> toSignalSet(List<DemandResponseEventSignalDto> src) {
		if (src == null) {
			return null;
		}
		Set<DemandResponseEventSignal> dst = new LinkedHashSet<>();
		for (DemandResponseEventSignalDto signal : src) {
			dst.add(toSignal(signal));
		}
		return dst;
	}

	/**
	 * event 역참조는 여기서 채우지 않는다.
	 * 저장 직전에 호출부가 setEvent 로 실제 영속 엔티티를 꽂아 준다.
	 */
	private DemandResponseEventSignal toSignal(DemandResponseEventSignalDto src) {
		if (src == null) {
			return null;
		}
		DemandResponseEventSignal dst = new DemandResponseEventSignal();
		// 앞뒤 공백만 걷고, 빈 문자열은 없는 것으로 본다(순번을 쓴다)
		String signalId = src.getSignalId() == null ? null : src.getSignalId().trim();
		dst.setSignalId(signalId == null || signalId.isEmpty() ? null : signalId);
		dst.setSignalName(src.getSignalName());
		dst.setSignalType(src.getSignalType());
		dst.setCurrentValue(src.getCurrentValue());
		dst.setItemBase(toItemBase(src.getItemBase()));
		dst.setIntervals(toIntervalList(src.getIntervals()));
		dst.setTargets(toTargetList(src.getTargets()));
		return dst;
	}

	private List<DemandResponseEventSignalIntervalDto> toIntervalDtoList(
			List<DemandResponseEventSignalInterval> src) {
		if (src == null) {
			return null;
		}
		List<DemandResponseEventSignalIntervalDto> dst = new ArrayList<>();
		for (DemandResponseEventSignalInterval interval : src) {
			DemandResponseEventSignalIntervalDto item = new DemandResponseEventSignalIntervalDto();
			item.setValue(interval.getValue());
			item.setDuration(interval.getDuration());
			dst.add(item);
		}
		return dst;
	}

	private List<DemandResponseEventSignalInterval> toIntervalList(
			List<DemandResponseEventSignalIntervalDto> src) {
		if (src == null) {
			return null;
		}
		List<DemandResponseEventSignalInterval> dst = new ArrayList<>();
		for (DemandResponseEventSignalIntervalDto interval : src) {
			DemandResponseEventSignalInterval item = new DemandResponseEventSignalInterval();
			item.setValue(interval.getValue());
			item.setDuration(interval.getDuration());
			dst.add(item);
		}
		return dst;
	}

	/**
	 * 엔티티 ItemBase 에는 attributes 가 더 있는데 DTO 에는 없다.
	 * dozer 도 이름이 없으면 건너뛰었으므로 그대로 둔다.
	 */
	private ItemBaseDto toItemBaseDto(ItemBase src) {
		if (src == null) {
			return null;
		}
		ItemBaseDto dst = new ItemBaseDto();
		dst.setItemDescription(src.getItemDescription());
		dst.setItemUnits(src.getItemUnits());
		dst.setSiScaleCode(src.getSiScaleCode());
		dst.setXmlType(src.getXmlType());
		return dst;
	}

	private ItemBase toItemBase(ItemBaseDto src) {
		if (src == null) {
			return null;
		}
		ItemBase dst = new ItemBase();
		dst.setItemDescription(src.getItemDescription());
		dst.setItemUnits(src.getItemUnits());
		dst.setSiScaleCode(src.getSiScaleCode());
		dst.setXmlType(src.getXmlType());
		return dst;
	}

	private List<TargetDto> toTargetDtoList(List<Target> src) {
		if (src == null) {
			return null;
		}
		List<TargetDto> dst = new ArrayList<>();
		for (Target target : src) {
			dst.add(new TargetDto(target.getTargetType(), target.getTargetId()));
		}
		return dst;
	}

	private List<Target> toTargetList(List<TargetDto> src) {
		if (src == null) {
			return null;
		}
		List<Target> dst = new ArrayList<>();
		for (TargetDto target : src) {
			dst.add(new Target(target.getTargetType(), target.getTargetId()));
		}
		return dst;
	}

}
