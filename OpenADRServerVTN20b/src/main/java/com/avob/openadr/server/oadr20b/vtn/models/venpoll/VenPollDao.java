package com.avob.openadr.server.oadr20b.vtn.models.venpoll;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenPollDao extends JpaRepository<VenPoll, Long> {

	@Query(value = "select poll from VenPoll poll inner join poll.ven ven where poll.ven.username = :venUsername")
	public List<VenPoll> findByVenUsername(@Param("venUsername") String venUsername, Pageable pageable);

	@Modifying
	public void deleteByVenUsername(@Param("venUsername") String venUsername);

}
