package com.avob.openadr.server.oadr20b.vtn.models.venreport.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportRequestDao<T extends ReportRequest> extends JpaRepository<T, Long> {

	public T findOneByReportRequestId(String reportRequestId);

}
