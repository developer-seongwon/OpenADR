package com.avob.openadr.server.oadr20b.vtn.service.report;

import java.util.List;

import jakarta.annotation.Resource;

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

	public List<OtherReportDataFloat> findByReportSpecifierIdAndRid(String venId, String reportSpecifierId,
			String rid) {
		return otherReportDataDao.findByVenIdAndReportSpecifierIdAndRid(venId, reportSpecifierId, rid);
	}

	@Override
	public JpaRepository<OtherReportDataFloat, Long> getDao() {
		return otherReportDataDao;
	}
}
