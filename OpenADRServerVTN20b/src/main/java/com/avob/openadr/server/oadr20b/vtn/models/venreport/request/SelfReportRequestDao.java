package com.avob.openadr.server.oadr20b.vtn.models.venreport.request;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.avob.openadr.server.common.vtn.models.ven.Ven;

public interface SelfReportRequestDao extends ReportRequestDao<SelfReportRequest> {

    public List<SelfReportRequest> findByTargetAndReportRequestIdIn(Ven ven, List<String> reportRequestId);
    
    public List<SelfReportRequest> findByTarget(Ven ven);

    // VEN 삭제 시 뒷정리용. selfreportrequest.ven_id 가 VEN 을 붙들고 있다
    @Transactional(readOnly = false)
    public void deleteByTarget(Ven ven);

}
