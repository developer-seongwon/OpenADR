package com.avob.openadr.server.common.vtn.models.vencredential;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface VenCredentialDao extends JpaRepository<VenCredential, Long> {

	public VenCredential findOneByVenId(Long venId);

	public boolean existsByVenId(Long venId);

	@Modifying
	@Query("delete from VenCredential c where c.venId = :venId")
	public void deleteByVenId(@Param("venId") Long venId);

}
