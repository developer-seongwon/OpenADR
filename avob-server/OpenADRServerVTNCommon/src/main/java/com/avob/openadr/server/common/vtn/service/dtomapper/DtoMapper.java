package com.avob.openadr.server.common.vtn.service.dtomapper;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEvent;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventReadDto;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.dto.DemandResponseEventUpdateDto;
import com.avob.openadr.server.common.vtn.models.user.AbstractUser;
import com.avob.openadr.server.common.vtn.models.user.AbstractUserWithRoleDto;
import com.avob.openadr.server.common.vtn.models.user.OadrApp;
import com.avob.openadr.server.common.vtn.models.user.OadrAppDto;
import com.avob.openadr.server.common.vtn.models.user.OadrUser;
import com.avob.openadr.server.common.vtn.models.user.OadrUserDto;
import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.models.ven.VenCreateDto;
import com.avob.openadr.server.common.vtn.models.ven.VenDto;
import com.avob.openadr.server.common.vtn.models.vendemandresponseevent.VenDemandResponseEvent;
import com.avob.openadr.server.common.vtn.models.vendemandresponseevent.VenDemandResponseEventDto;
import com.avob.openadr.server.common.vtn.models.vengroup.VenGroup;
import com.avob.openadr.server.common.vtn.models.vengroup.VenGroupDto;
import com.avob.openadr.server.common.vtn.models.venmarketcontext.VenMarketContext;
import com.avob.openadr.server.common.vtn.models.venmarketcontext.VenMarketContextDto;
import com.avob.openadr.server.common.vtn.models.venresource.VenResource;
import com.avob.openadr.server.common.vtn.models.venresource.VenResourceDto;

/**
 * 매핑 진입점. 호출부 40여 군데가 map 과 mapList 를 쓰고 있어서 시그니처는 그대로 두고
 * 안쪽만 dozer 에서 손으로 쓴 매퍼로 갈아 끼웠다.
 *
 * 대상 타입으로 갈라 주는 이 switch 는 dozer 가 리플렉션으로 하던 일을 대신하는 것뿐이라
 * 그리 좋은 모양은 아니다. 각 매퍼에 타입이 분명한 메서드가 따로 있으니
 * 새로 쓰는 코드는 VenDtoMapper.toVenDto 처럼 직접 부르는 편이 낫고,
 * 기존 호출부도 하나씩 그쪽으로 옮기면 이 클래스는 결국 사라진다.
 *
 * 여기서 모르는 조합이 들어오면 예외를 던진다.
 * dozer 는 이름이 안 맞으면 조용히 빈 객체를 돌려줬는데, 그게 원인을 찾기 제일 어려웠다.
 */
@Service
public class DtoMapper {

	@Resource
	private UserDtoMapper userDtoMapper;

	@Resource
	private VenDtoMapper venDtoMapper;

	@Resource
	private DemandResponseEventDtoMapper demandResponseEventDtoMapper;

	public <T> T map(Object src, Class<T> klass) {
		if (src == null) {
			return null;
		}
		return klass.cast(doMap(src, klass));
	}

	public <T> List<T> mapList(Iterable<?> src, Class<T> klass) {
		List<T> list = new ArrayList<>();
		if (src == null) {
			return list;
		}
		for (Object obj : src) {
			list.add(map(obj, klass));
		}
		return list;
	}

	/**
	 * 하위 모듈이 자기 타입을 덧붙일 수 있게 열어 둔다.
	 * VTN20b 의 Oadr20bDtoMapper 가 2.0b 고유 타입을 먼저 처리하고
	 * 모르는 조합만 super 로 넘긴다.
	 */
	protected Object doMap(Object src, Class<?> klass) {

		if (VenDto.class.equals(klass)) {
			return venDtoMapper.toVenDto((Ven) src);
		}
		if (VenCreateDto.class.equals(klass)) {
			return venDtoMapper.toVenCreateDto((Ven) src);
		}
		if (VenResourceDto.class.equals(klass)) {
			return venDtoMapper.toVenResourceDto((VenResource) src);
		}
		if (VenGroupDto.class.equals(klass)) {
			return venDtoMapper.toVenGroupDto((VenGroup) src);
		}
		if (VenMarketContextDto.class.equals(klass)) {
			return venDtoMapper.toVenMarketContextDto((VenMarketContext) src);
		}
		if (VenDemandResponseEventDto.class.equals(klass)) {
			return venDtoMapper.toVenDemandResponseEventDto((VenDemandResponseEvent) src);
		}

		if (AbstractUserWithRoleDto.class.equals(klass)) {
			return userDtoMapper.toAbstractUserWithRoleDto((AbstractUser) src);
		}
		if (OadrUserDto.class.equals(klass)) {
			return userDtoMapper.toOadrUserDto((OadrUser) src);
		}
		if (OadrAppDto.class.equals(klass)) {
			return userDtoMapper.toOadrAppDto((OadrApp) src);
		}

		if (DemandResponseEventReadDto.class.equals(klass)) {
			return demandResponseEventDtoMapper.toReadDto((DemandResponseEvent) src);
		}
		if (DemandResponseEvent.class.equals(klass)) {
			// 생성 요청과 수정 요청이 같은 엔티티로 들어온다. 들고 있는 필드가 달라 나눠 부른다
			if (src instanceof DemandResponseEventUpdateDto) {
				return demandResponseEventDtoMapper.toEntity((DemandResponseEventUpdateDto) src);
			}
			return demandResponseEventDtoMapper.toEntity((DemandResponseEventDto) src);
		}

		throw new IllegalArgumentException(
				"등록되지 않은 매핑이다: " + src.getClass().getName() + " 에서 " + klass.getName());
	}

}
