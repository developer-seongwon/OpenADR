package com.avob.openadr.server.common.vtn.models.venmarketcontext;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.avob.openadr.server.common.vtn.models.ven.Ven;

@Transactional
public interface VenMarketContextDao extends JpaRepository<VenMarketContext, Long> {

	public VenMarketContext findOneByName(String name);

	public List<VenMarketContext> findByNameIn(List<String> name);
	
	public List<VenMarketContext> findByVensContaining(Ven ven);

}
