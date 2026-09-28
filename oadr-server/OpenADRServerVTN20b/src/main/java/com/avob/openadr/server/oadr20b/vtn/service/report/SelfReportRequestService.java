package com.avob.openadr.server.oadr20b.vtn.service.report;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.SelfReportRequest;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.SelfReportRequestDao;
import com.avob.openadr.server.oadr20b.vtn.service.GenericService;

@Service
public class SelfReportRequestService extends GenericService<SelfReportRequest> {

    @Resource
    private SelfReportRequestDao selfReportRequestDao;

    public List<SelfReportRequest> findByTarget(Ven ven) {
        return selfReportRequestDao.findByTarget(ven);
    }

    public List<SelfReportRequest> findByTargetAndReportRequestId(Ven ven, List<String> reportRequestId) {
        return selfReportRequestDao.findByTargetAndReportRequestIdIn(ven, reportRequestId);
    }

    @Override
    public JpaRepository<SelfReportRequest, Long> getDao() {
        return selfReportRequestDao;
    }

}
