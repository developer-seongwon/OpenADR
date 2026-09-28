package com.avob.openadr.server.oadr20b.vtn.models.venreport.capability;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportCapabilityDao<T extends ReportCapability> extends JpaRepository<T, Long> {

	public List<T> findByReportSpecifierId(String reportSpecifierId);

	public List<T> findByReportSpecifierIdStartingWith(String reportSpecifierId);

}
