package com.avob.openadr.server.oadr20b.vtn.models.venreport.capability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportCapabilityDescriptionDao<T extends ReportCapabilityDescription> extends JpaRepository<T, Long> {

	public T findByRid(String rid);

}
