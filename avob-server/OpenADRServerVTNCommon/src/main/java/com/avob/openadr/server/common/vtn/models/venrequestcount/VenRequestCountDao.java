package com.avob.openadr.server.common.vtn.models.venrequestcount;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

/**
 * Ven request count model database interface
 * 
 * @author bertrand
 *
 */
@Component
public interface VenRequestCountDao extends JpaRepository<VenRequestCount, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select r from VenRequestCount r WHERE r.venId = :venId")
    public VenRequestCount findOneAndLock(@Param("venId") String venId);

}
