package com.avob.openadr.server.common.vtn.models.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OadrAppDao extends JpaRepository<OadrApp, Long> {

	public List<OadrApp> findByUsernameIn(List<String> username);
	
	public List<OadrApp> findAll();

	public OadrApp findOneByUsername(String username);

}
