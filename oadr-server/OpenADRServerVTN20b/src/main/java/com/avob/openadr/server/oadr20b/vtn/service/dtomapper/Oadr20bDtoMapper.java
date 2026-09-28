package com.avob.openadr.server.oadr20b.vtn.service.dtomapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.models.ItemBase;
import com.avob.openadr.server.common.vtn.models.ItemBaseDto;
import com.avob.openadr.server.common.vtn.models.Target;
import com.avob.openadr.server.common.vtn.models.TargetDto;
import com.avob.openadr.server.common.vtn.models.user.AbstractUser;
import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.service.dtomapper.DtoMapper;
import com.avob.openadr.server.oadr20b.vtn.controller.VtnConfigurationDto;
import com.avob.openadr.server.oadr20b.vtn.models.venopt.VenOpt;
import com.avob.openadr.server.oadr20b.vtn.models.venopt.VenOptDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapability;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescription;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescriptionDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.ReportCapability;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.ReportCapabilityDescription;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.ReportCapabilityDescriptionDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.ReportCapabilityDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.SamplingRate;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.SamplingRateDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.VenReportCapabilityDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.VenReportDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloat;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloatDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataKeyToken;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataKeyTokenDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataPayloadResourceStatus;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataPayloadResourceStatusDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.ReportData;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.ReportDataDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequest;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestSpecifier;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestSpecifierDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.ReportRequest;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.ReportRequestDto;

/**
 * 2.0b 쪽 엔티티를 DTO 로 옮긴다.
 *
 * 예전에는 VTNCommon 의 DtoMapper 를 상속해서 dozer 매퍼 인스턴스에 매핑 규칙을 덧붙였다.
 * 그런데 그 dozer 는 구 네임스페이스인 org.dozer 였고 jakarta 로 넘어오지 못했다.
 * VTNCommon 에서 dozer 를 걷어내면서 상속하던 mapper 필드와 init() 도 같이 사라졌다.
 *
 * 지금은 공통 타입은 부모의 map 에 그대로 넘기고, 2.0b 고유 타입만 여기서 직접 옮긴다.
 * 엔티티를 식별자로 줄이는 규칙(ven 을 username 으로, capability 를 reportSpecifierId 로 등)은
 * 예전 커스텀 컨버터가 하던 일을 그대로 옮겨 온 것이다.
 */
@Service
public class Oadr20bDtoMapper extends DtoMapper {

	@Override
	protected Object doMap(Object src, Class<?> klass) {

		if (VenOptDto.class.equals(klass)) {
			return toVenOptDto((VenOpt) src);
		}
		if (OtherReportCapabilityDto.class.equals(klass)) {
			return toOtherReportCapabilityDto((OtherReportCapability) src);
		}
		if (OtherReportCapabilityDescriptionDto.class.equals(klass)) {
			return toOtherReportCapabilityDescriptionDto((OtherReportCapabilityDescription) src);
		}
		if (OtherReportRequestDto.class.equals(klass)) {
			return toOtherReportRequestDto((OtherReportRequest) src);
		}
		if (OtherReportRequestSpecifierDto.class.equals(klass)) {
			return toOtherReportRequestSpecifierDto((OtherReportRequestSpecifier) src);
		}
		if (ReportCapabilityDto.class.equals(klass)) {
			return toReportCapabilityDto((ReportCapability) src);
		}
		if (ReportCapabilityDescriptionDto.class.equals(klass)) {
			return toReportCapabilityDescriptionDto((ReportCapabilityDescription) src);
		}
		if (ReportRequestDto.class.equals(klass)) {
			return toReportRequestDto((ReportRequest) src);
		}
		if (VenReportDto.class.equals(klass)) {
			return toVenReportDto((Ven) src);
		}
		if (VenReportCapabilityDto.class.equals(klass)) {
			return toVenReportCapabilityDto((OtherReportCapability) src);
		}
		if (OtherReportDataFloatDto.class.equals(klass)) {
			return toOtherReportDataFloatDto((OtherReportDataFloat) src);
		}
		if (OtherReportDataKeyTokenDto.class.equals(klass)) {
			return toOtherReportDataKeyTokenDto((OtherReportDataKeyToken) src);
		}
		if (OtherReportDataPayloadResourceStatusDto.class.equals(klass)) {
			return toOtherReportDataPayloadResourceStatusDto((OtherReportDataPayloadResourceStatus) src);
		}
		if (VtnConfigurationDto.class.equals(klass)) {
			return toVtnConfigurationDto((com.avob.openadr.server.common.vtn.VtnConfig) src);
		}

		// 2.0b 가 모르는 조합은 공통 매퍼에게 넘긴다
		return super.doMap(src, klass);
	}

	/**
	 * VTN 설정을 화면용 DTO 로 옮긴다.
	 *
	 * VtnConfig 에 같은 이름의 getter 가 없는 필드는 채우지 않는다.
	 * host, oadrVersion, supportCertificateGeneration, xsdValidation,
	 * xmlSignatureReplayProtectSecond, saveVenDate 가 그렇고,
	 * 이름으로만 찾던 예전 dozer 매핑에서도 비어 있었다. 동작을 바꾸지 않는다.
	 */
	public VtnConfigurationDto toVtnConfigurationDto(com.avob.openadr.server.common.vtn.VtnConfig src) {
		if (src == null) {
			return null;
		}
		VtnConfigurationDto dst = new VtnConfigurationDto();
		dst.setVtnId(src.getVtnId());
		dst.setSupportPush(src.getSupportPush());
		dst.setSupportUnsecuredHttpPush(src.getSupportUnsecuredHttpPush());
		dst.setPullFrequencySeconds(src.getPullFrequencySeconds());
		dst.setPort(src.getPort());
		dst.setContextPath(src.getContextPath());
		return dst;
	}

	public VenOptDto toVenOptDto(VenOpt src) {
		if (src == null) {
			return null;
		}
		VenOptDto dst = new VenOptDto();
		dst.setId(src.getId());
		dst.setOptId(src.getOptId());
		dst.setStart(src.getStart());
		dst.setEnd(src.getEnd());
		dst.setOpt(src.getOpt());
		dst.setVenId(src.getVen() == null ? null : src.getVen().getUsername());
		dst.setMarketContext(src.getMarketContext() == null ? null : src.getMarketContext().getName());
		dst.setEventId(src.getEvent() == null ? null : String.valueOf(src.getEvent().getId()));
		dst.setResourceId(src.getVenResource() == null ? null : src.getVenResource().getId());
		return dst;
	}

	/** ReportCapability 의 공통 필드. 하위 DTO 들이 전부 이걸 상속한다 */
	private void copyReportCapability(ReportCapability src, ReportCapabilityDto dst) {
		dst.setId(src.getId() == null ? 0L : src.getId());
		dst.setReportId(src.getReportId());
		dst.setReportSpecifierId(src.getReportSpecifierId());
		dst.setReportName(src.getReportName());
		dst.setStart(src.getStart());
		dst.setDuration(src.getDuration());
		dst.setCreatedDatetime(src.getCreatedDatetime());
	}

	public ReportCapabilityDto toReportCapabilityDto(ReportCapability src) {
		if (src == null) {
			return null;
		}
		ReportCapabilityDto dst = new ReportCapabilityDto();
		copyReportCapability(src, dst);
		return dst;
	}

	public OtherReportCapabilityDto toOtherReportCapabilityDto(OtherReportCapability src) {
		if (src == null) {
			return null;
		}
		OtherReportCapabilityDto dst = new OtherReportCapabilityDto();
		copyReportCapability(src, dst);
		dst.setVenId(src.getSource() == null ? null : src.getSource().getUsername());
		return dst;
	}

	/**
	 * VEN 하나가 가진 리포트 능력.
	 *
	 * descriptions 는 여기서 채우지 않는다. 엔티티의 description 컬렉션이 지연 로딩이라
	 * 세션 밖에서 건드리면 LazyInitializationException 이 난다.
	 * 호출부인 Oadr20bVTNEiReportService 가 방금 처리한 description 들로 직접 세팅한다.
	 * 예전 dozer 매핑에서도 이 필드는 매퍼가 채우지 않았다.
	 */
	public VenReportCapabilityDto toVenReportCapabilityDto(OtherReportCapability src) {
		if (src == null) {
			return null;
		}
		VenReportCapabilityDto dst = new VenReportCapabilityDto();
		copyReportCapability(src, dst);
		return dst;
	}

	public VenReportDto toVenReportDto(Ven src) {
		if (src == null) {
			return null;
		}
		VenReportDto dst = new VenReportDto();
		dst.setId(src.getId());
		dst.setUsername(src.getUsername());
		return dst;
	}

	private void copyReportCapabilityDescription(ReportCapabilityDescription src, ReportCapabilityDescriptionDto dst) {
		dst.setId(src.getId() == null ? 0L : src.getId());
		dst.setRid(src.getRid());
		dst.setReportType(src.getReportType());
		dst.setReadingType(src.getReadingType());
		dst.setSamplingRate(toSamplingRateDto(src.getSamplingRate()));
		dst.setItemBase(toItemBaseDto(src.getItemBase()));
		dst.setEiDatasource(toTargetDtoList(src.getEiDatasource()));
		dst.setEiSubject(toTargetDtoList(src.getEiSubject()));
	}

	public ReportCapabilityDescriptionDto toReportCapabilityDescriptionDto(ReportCapabilityDescription src) {
		if (src == null) {
			return null;
		}
		ReportCapabilityDescriptionDto dst = new ReportCapabilityDescriptionDto();
		copyReportCapabilityDescription(src, dst);
		return dst;
	}

	public OtherReportCapabilityDescriptionDto toOtherReportCapabilityDescriptionDto(
			OtherReportCapabilityDescription src) {
		if (src == null) {
			return null;
		}
		OtherReportCapabilityDescriptionDto dst = new OtherReportCapabilityDescriptionDto();
		copyReportCapabilityDescription(src, dst);
		OtherReportCapability capability = src.getOtherReportCapability();
		if (capability != null) {
			dst.setReportSpecifierId(capability.getReportSpecifierId());
			dst.setVenId(capability.getSource() == null ? null : capability.getSource().getUsername());
		}
		return dst;
	}

	private void copyReportRequest(ReportRequest src, ReportRequestDto dst) {
		dst.setId(src.getId());
		dst.setReportRequestId(src.getReportRequestId());
		dst.setGranularity(src.getGranularity());
		dst.setReportBackDuration(src.getReportBackDuration());
		dst.setStart(src.getStart());
		dst.setEnd(src.getEnd());
		dst.setAcked(src.isAcked());
		dst.setCreatedDatetime(src.getCreatedDatetime());
	}

	public ReportRequestDto toReportRequestDto(ReportRequest src) {
		if (src == null) {
			return null;
		}
		ReportRequestDto dst = new ReportRequestDto();
		copyReportRequest(src, dst);
		return dst;
	}

	public OtherReportRequestDto toOtherReportRequestDto(OtherReportRequest src) {
		if (src == null) {
			return null;
		}
		OtherReportRequestDto dst = new OtherReportRequestDto();
		copyReportRequest(src, dst);
		OtherReportCapability capability = src.getOtherReportCapability();
		dst.setReportSpecifierId(capability == null ? null : capability.getReportSpecifierId());
		dst.setVenId(src.getSource() == null ? null : src.getSource().getUsername());
		AbstractUser requestor = src.getRequestor();
		dst.setRequestorUsername(requestor == null ? null : requestor.getUsername());
		return dst;
	}

	public OtherReportRequestSpecifierDto toOtherReportRequestSpecifierDto(OtherReportRequestSpecifier src) {
		if (src == null) {
			return null;
		}
		OtherReportRequestSpecifierDto dst = new OtherReportRequestSpecifierDto();
		dst.setId(src.getId());
		dst.setArchived(src.getArchived());
		dst.setLastUpdateDatetime(src.getLastUpdateDatetime());
		dst.setLastUpdateValue(src.getLastUpdateValue());
		dst.setReportRequestId(src.getRequest() == null ? null : src.getRequest().getReportRequestId());
		dst.setRid(src.getOtherReportCapabilityDescription() == null ? null
				: src.getOtherReportCapabilityDescription().getRid());
		return dst;
	}

	private void copyReportData(ReportData src, ReportDataDto dst) {
		dst.setId(src.getId());
		dst.setReportSpecifierId(src.getReportSpecifierId());
		dst.setReportRequestId(src.getReportRequestId());
		dst.setRid(src.getRid());
		dst.setConfidence(src.getConfidence());
		dst.setAccuracy(src.getAccuracy());
		dst.setStart(src.getStart());
		dst.setDuration(src.getDuration());
	}

	public OtherReportDataFloatDto toOtherReportDataFloatDto(OtherReportDataFloat src) {
		if (src == null) {
			return null;
		}
		OtherReportDataFloatDto dst = new OtherReportDataFloatDto();
		copyReportData(src, dst);
		dst.setValue(src.getValue());
		return dst;
	}

	public OtherReportDataKeyTokenDto toOtherReportDataKeyTokenDto(OtherReportDataKeyToken src) {
		if (src == null) {
			return null;
		}
		OtherReportDataKeyTokenDto dst = new OtherReportDataKeyTokenDto();
		copyReportData(src, dst);
		dst.setTokens(src.getTokens());
		return dst;
	}

	public OtherReportDataPayloadResourceStatusDto toOtherReportDataPayloadResourceStatusDto(
			OtherReportDataPayloadResourceStatus src) {
		if (src == null) {
			return null;
		}
		OtherReportDataPayloadResourceStatusDto dst = new OtherReportDataPayloadResourceStatusDto();
		copyReportData(src, dst);
		dst.setOadrCapacityMin(src.getOadrCapacityMin());
		dst.setOadrCapacityMax(src.getOadrCapacityMax());
		dst.setOadrCapacityCurrent(src.getOadrCapacityCurrent());
		dst.setOadrCapacityNormal(src.getOadrCapacityNormal());
		dst.setOadrLevelOffsetMin(src.getOadrLevelOffsetMin());
		dst.setOadrLevelOffsetMax(src.getOadrLevelOffsetMax());
		dst.setOadrLevelOffsetCurrent(src.getOadrLevelOffsetCurrent());
		dst.setOadrLevelOffsetNormal(src.getOadrLevelOffsetNormal());
		dst.setOadrPercentOffsetMin(src.getOadrPercentOffsetMin());
		dst.setOadrPercentOffsetMax(src.getOadrPercentOffsetMax());
		dst.setOadrPercentOffsetCurrent(src.getOadrPercentOffsetCurrent());
		dst.setOadrPercentOffsetNormal(src.getOadrPercentOffsetNormal());
		dst.setOadrSetPointMin(src.getOadrSetPointMin());
		dst.setOadrSetPointMax(src.getOadrSetPointMax());
		dst.setOadrSetPointCurrent(src.getOadrSetPointCurrent());
		dst.setOadrSetPointNormal(src.getOadrSetPointNormal());
		return dst;
	}

	private SamplingRateDto toSamplingRateDto(SamplingRate src) {
		if (src == null) {
			return null;
		}
		SamplingRateDto dst = new SamplingRateDto();
		dst.setOadrMaxPeriod(src.getOadrMaxPeriod());
		dst.setOadrMinPeriod(src.getOadrMinPeriod());
		dst.setOadrOnChange(src.isOadrOnChange() != null && src.isOadrOnChange());
		return dst;
	}

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

}
