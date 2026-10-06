package com.avob.openadr.server.common.vtn.service.dtomapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

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
 * VEN 과 그 주변 엔티티를 DTO 로 옮긴다. dozer 를 걷어내고 손으로 쓴 매퍼다.
 *
 * 이 매퍼는 DB 를 건드리지 않는다.
 * 예전 VenMapper, VenResourceMapper 는 문자열이나 id 를 받아 엔티티를 다시 조회하는
 * 역방향 변환을 들고 있었는데, 실제 호출부를 전부 뒤져 보니 쓰이는 데가 없었다.
 * 그 역방향 때문에 매퍼가 VenService 를 물고 VenService 가 다시 매퍼를 물던
 * 순환 참조도 같이 사라진다.
 */
@Service
public class VenDtoMapper {

	@Resource
	private UserDtoMapper userDtoMapper;

	public VenDto toVenDto(Ven src) {
		if (src == null) {
			return null;
		}
		VenDto dst = new VenDto();
		userDtoMapper.copyCommon(src, dst);
		dst.setOadrName(src.getOadrName());
		dst.setOadrProfil(src.getOadrProfil());
		dst.setTransport(src.getTransport());
		dst.setPushUrl(src.getPushUrl());
		dst.setHttpPullModel(src.getHttpPullModel());
		dst.setLastUpdateDatetime(src.getLastUpdateDatetime());
		dst.setRegistrationId(src.getRegistrationId());
		dst.setReportOnly(src.getReportOnly());
		dst.setXmlSignature(src.getXmlSignature());
		return dst;
	}

	/**
	 * VenCreateDto 에는 marketContexts, groups, resources 라는 문자열 목록이 있는데
	 * 엔티티 쪽 이름은 venMarketContexts, venGroups, venResources 라 이름이 다르다.
	 * dozer 도 이름으로만 찾았으니 예전에도 이 셋은 비어 있었다. 동작을 바꾸지 않으려고
	 * 여기서도 채우지 않는다. 채워야 한다면 그건 버그 수정이라 따로 다뤄야 한다.
	 */
	public VenCreateDto toVenCreateDto(Ven src) {
		if (src == null) {
			return null;
		}
		VenCreateDto dst = new VenCreateDto();
		dst.setUsername(src.getUsername());
		dst.setCommonName(src.getCommonName());
		dst.setAuthenticationType(src.getAuthenticationType());
		dst.setOadrName(src.getOadrName());
		dst.setOadrProfil(src.getOadrProfil());
		dst.setTransport(src.getTransport());
		dst.setPushUrl(src.getPushUrl());
		dst.setHttpPullModel(src.getHttpPullModel());
		dst.setReportOnly(src.getReportOnly());
		dst.setXmlSignature(src.getXmlSignature());
		return dst;
	}

	/**
	 * 새 객체를 만들지 않고 이미 있는 Ven 에 덮어쓴다.
	 * VenService.prepare 가 username 과 비밀번호를 먼저 세팅해 둔 인스턴스를 넘기기 때문이다.
	 * dozer 는 기본 설정에서 null 도 그대로 덮어썼으므로 여기서도 null 검사 없이 덮는다.
	 * password 는 엔티티에 같은 이름의 필드가 없어 예전에도 옮겨지지 않았다.
	 * 비밀번호 처리는 prepare 안에서 따로 하고 있다.
	 */
	public void copyToVen(VenCreateDto src, Ven dst) {
		if (src == null || dst == null) {
			return;
		}
		dst.setUsername(src.getUsername());
		dst.setCommonName(src.getCommonName());
		dst.setAuthenticationType(src.getAuthenticationType());
		dst.setOadrName(src.getOadrName());
		dst.setOadrProfil(src.getOadrProfil());
		dst.setTransport(src.getTransport());
		dst.setPushUrl(src.getPushUrl());
		dst.setHttpPullModel(src.getHttpPullModel());
		dst.setReportOnly(src.getReportOnly());
		dst.setXmlSignature(src.getXmlSignature());
	}

	public VenResourceDto toVenResourceDto(VenResource src) {
		if (src == null) {
			return null;
		}
		VenResourceDto dst = new VenResourceDto();
		dst.setId(src.getId());
		dst.setName(src.getName());
		return dst;
	}

	public VenGroupDto toVenGroupDto(VenGroup src) {
		if (src == null) {
			return null;
		}
		VenGroupDto dst = new VenGroupDto();
		dst.setId(src.getId());
		dst.setName(src.getName());
		dst.setDescription(src.getDescription());
		return dst;
	}

	public VenMarketContextDto toVenMarketContextDto(VenMarketContext src) {
		if (src == null) {
			return null;
		}
		VenMarketContextDto dst = new VenMarketContextDto();
		dst.setId(src.getId());
		dst.setName(src.getName());
		dst.setDescription(src.getDescription());
		dst.setColor(src.getColor());
		return dst;
	}

	/**
	 * ven 과 event 는 엔티티 통째로가 아니라 식별자만 DTO 로 나간다.
	 * 예전 DtoMapper 가 커스텀 컨버터로 등록해 두던 규칙을 그대로 옮겼다.
	 * event 쪽에서 descriptor 가 null 이면 id 도 내보내지 않는데,
	 * 아직 저장이 끝나지 않은 이벤트를 거르려는 의도로 보인다.
	 */
	public VenDemandResponseEventDto toVenDemandResponseEventDto(VenDemandResponseEvent src) {
		if (src == null) {
			return null;
		}
		VenDemandResponseEventDto dst = new VenDemandResponseEventDto();
		dst.setId(src.getId());
		dst.setLastSentModificationNumber(src.getLastSentModificationNumber());
		dst.setLastUpdateDatetime(src.getLastUpdateDatetime());
		dst.setVenOpt(src.getVenOpt());
		dst.setVenId(src.getVen() == null ? null : src.getVen().getUsername());
		if (src.getEvent() != null && src.getEvent().getDescriptor() != null) {
			dst.setEventId(String.valueOf(src.getEvent().getId()));
		}
		return dst;
	}

}
