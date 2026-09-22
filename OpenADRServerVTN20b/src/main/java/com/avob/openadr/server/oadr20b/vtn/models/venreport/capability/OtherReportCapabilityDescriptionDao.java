package com.avob.openadr.server.oadr20b.vtn.models.venreport.capability;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.avob.openadr.server.common.vtn.models.ven.Ven;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OtherReportCapabilityDescriptionDao
		extends ReportCapabilityDescriptionDao<OtherReportCapabilityDescription>,
		JpaSpecificationExecutor<OtherReportCapabilityDescription> {

	public List<OtherReportCapabilityDescription> findByOtherReportCapabilityAndRidIn(
			OtherReportCapability otherReportCapability, List<String> rid);

	public OtherReportCapabilityDescription findOneByOtherReportCapabilityAndRid(
			OtherReportCapability otherReportCapability, String rid);

	// ORDER BY 가 없으면 행 순서는 DB 가 정한다. H2 는 대체로 삽입 순서로 돌려줬지만
	// PostgreSQL 은 보장하지 않는다. 화면에 목록으로 나가는 자리라 순서를 고정한다
	public List<OtherReportCapabilityDescription> findByOtherReportCapabilityOrderByIdAsc(
			OtherReportCapability otherReportCapability);

	public List<OtherReportCapabilityDescription> findByOtherReportCapabilityIn(
			List<OtherReportCapability> otherReportCapabilities);

	// VEN 삭제 시 뒷정리용
	@Transactional(readOnly = false)
	public void deleteByOtherReportCapabilitySource(Ven source);

}
