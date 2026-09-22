package com.avob.openadr.server.oadr20b.vtn.models.venreport.request;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapability;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescription;

public interface OtherReportRequestSpecifierDao extends JpaRepository<OtherReportRequestSpecifier, Long>,
		JpaSpecificationExecutor<OtherReportRequestSpecifier> {

	@Transactional
	public List<OtherReportRequestSpecifier> findByRequest(OtherReportRequest request);

	@Transactional
	public List<OtherReportRequestSpecifier> findByRequestReportRequestId(String reportRequestId);

	@Transactional
	public List<OtherReportRequestSpecifier> findByRequestSource(Ven source);

	@Transactional
	public List<OtherReportRequestSpecifier> findByRequestSourceAndRequestReportRequestIdIn(Ven source,
			List<String> reportRequestId);

	@Transactional
	public List<OtherReportRequestSpecifier> findByRequestSourceAndOtherReportCapabilityDescriptionRidIn(Ven source,
			List<String> rid);

	@Transactional
	public List<OtherReportRequestSpecifier> findByRequestSourceAndRequestReportRequestIdInAndOtherReportCapabilityDescriptionRidIn(
			Ven source, List<String> reportRequestId, List<String> rid);

	@Transactional(readOnly = false)
	public void deleteByOtherReportCapabilityDescriptionIn(Collection<OtherReportCapabilityDescription> descs);

	@Transactional(readOnly = false)
	public void deleteByRequestReportRequestId(String reportRequestId);

	@Transactional(readOnly = false)
	public void deleteByOtherReportCapabilityDescriptionRidInAndOtherReportCapabilityDescriptionOtherReportCapability(
			Collection<String> rids, OtherReportCapability otherReportCapability);

	@Transactional(readOnly = false)
	public void deleteByRequest(OtherReportRequest request);

	public Long countByOtherReportCapabilityDescriptionOtherReportCapability(
			OtherReportCapability otherReportCapability);

	@Transactional(readOnly = false)
	public void deleteByOtherReportCapabilityDescriptionOtherReportCapabilitySource(Ven ven);

	// 위는 description 을 거치므로 description 이 없는 행은 못 지운다.
	// VEN 을 지울 때는 그 VEN 의 요청에 달린 것을 전부 치운다
	@Transactional(readOnly = false)
	public void deleteByRequestSource(Ven source);
}
