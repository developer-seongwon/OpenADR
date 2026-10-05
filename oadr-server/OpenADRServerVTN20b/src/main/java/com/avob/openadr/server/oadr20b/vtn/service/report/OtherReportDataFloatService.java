package com.avob.openadr.server.oadr20b.vtn.service.report;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloat;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloatDao;
import com.avob.openadr.server.oadr20b.vtn.service.GenericService;

@Service
public class OtherReportDataFloatService extends GenericService<OtherReportDataFloat> {

	@Resource
	private OtherReportDataFloatDao otherReportDataDao;

	public List<OtherReportDataFloat> findByReportSpecifierId(String venId, String reportSpecifierId) {
		return otherReportDataDao.findByVenIdAndReportSpecifierId(venId, reportSpecifierId);
	}

	/** 리포트 요청 하나로 받은 값을 최근(start) 것부터 limit 건 */
	public List<OtherReportDataFloat> findRecentByReportRequestId(String venId, String reportRequestId, int limit) {
		return otherReportDataDao.findByVenIdAndReportRequestId(venId, reportRequestId,
				PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "start")));
	}

	public List<OtherReportDataFloat> findByReportSpecifierIdAndRid(String venId, String reportSpecifierId,
			String rid) {
		return otherReportDataDao.findByVenIdAndReportSpecifierIdAndRid(venId, reportSpecifierId, rid);
	}

	@Override
	public JpaRepository<OtherReportDataFloat, Long> getDao() {
		return otherReportDataDao;
	}
}
