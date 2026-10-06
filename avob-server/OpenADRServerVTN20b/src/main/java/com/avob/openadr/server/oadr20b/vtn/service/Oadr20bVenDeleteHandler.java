package com.avob.openadr.server.oadr20b.vtn.service;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.service.VenDeleteHandler;
import com.avob.openadr.server.oadr20b.vtn.models.venopt.VenOptDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescriptionDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloatDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataKeyTokenDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataPayloadResourceStatusDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestSpecifierDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.SelfReportRequestDao;

/**
 * VEN 을 지우기 전에 VTN20b 가 들고 있는 행을 치운다.
 *
 * VTNCommon 의 VenService 는 venresource 와 vendemandresponseevent 만 비운다.
 * 그런데 VTN20b 에도 VEN 을 참조하는 테이블이 있다.
 * otherreportcapability, otherreportrequest, selfreportrequest, venopt 가
 * 전부 ON DELETE NO ACTION 이라, 행이 남아 있으면 VEN 삭제가 외래키 위반으로 터진다.
 * 그동안은 리포트 명세를 한 번이라도 올린 VEN 은 화면에서도 API 로도 못 지웠다.
 *
 * 지우는 순서가 있다. 매달린 것부터 떼야 한다.
 *   otherreportrequestspecifier -> otherreportrequest
 *   otherreportcapabilitydescription -> otherreportcapability
 *
 * venpoll 은 ON DELETE CASCADE 라 DB 가 알아서 지운다.
 * 리포트 데이터 테이블들은 외래키가 아니라 username 문자열을 들고 있어서 삭제를
 * 막지는 않지만, 남겨 두면 같은 이름으로 VEN 을 다시 만들었을 때 옛 데이터가 섞인다.
 */
@Component
public class Oadr20bVenDeleteHandler implements VenDeleteHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(Oadr20bVenDeleteHandler.class);

	@Resource
	private OtherReportRequestSpecifierDao otherReportRequestSpecifierDao;

	@Resource
	private OtherReportRequestDao otherReportRequestDao;

	@Resource
	private OtherReportCapabilityDescriptionDao otherReportCapabilityDescriptionDao;

	@Resource
	private OtherReportCapabilityDao otherReportCapabilityDao;

	@Resource
	private SelfReportRequestDao selfReportRequestDao;

	@Resource
	private VenOptDao venOptDao;

	@Resource
	private OtherReportDataFloatDao otherReportDataFloatDao;

	@Resource
	private OtherReportDataKeyTokenDao otherReportDataKeyTokenDao;

	@Resource
	private OtherReportDataPayloadResourceStatusDao otherReportDataPayloadResourceStatusDao;

	@Override
	@Transactional
	public void onVenDelete(Ven ven) {

		LOGGER.debug("Clean oadr20b data for ven: " + ven.getUsername());

		// VEN 이 VTN 에게 주기로 한 리포트 요청
		otherReportRequestSpecifierDao.deleteByRequestSource(ven);
		otherReportRequestDao.deleteBySource(ven);

		// VEN 이 올린 리포트 명세
		otherReportCapabilityDescriptionDao.deleteByOtherReportCapabilitySource(ven);
		otherReportCapabilityDao.deleteBySource(ven);

		// VTN 이 자기 리포트를 이 VEN 에게 주기로 한 것
		selfReportRequestDao.deleteByTarget(ven);

		// opt 스케줄
		venOptDao.deleteByVen(ven);

		// 쌓인 리포트 데이터
		String venId = ven.getUsername();
		otherReportDataFloatDao.deleteByVenId(venId);
		otherReportDataKeyTokenDao.deleteByVenId(venId);
		otherReportDataPayloadResourceStatusDao.deleteByVenId(venId);
	}

}
