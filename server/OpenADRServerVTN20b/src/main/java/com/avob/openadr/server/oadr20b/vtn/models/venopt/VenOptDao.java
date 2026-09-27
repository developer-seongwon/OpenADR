package com.avob.openadr.server.oadr20b.vtn.models.venopt;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.avob.openadr.server.common.vtn.models.ven.Ven;

@Transactional(readOnly = true)
public interface VenOptDao extends JpaRepository<VenOpt, Long> {

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven where opt.ven.username = :venUsername and opt.start >= :start and opt.end < :end")
    public List<VenOpt> findScheduledOptBetween(@Param("venUsername") String venUsername, @Param("start") Long start,
            @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven where opt.ven.username = :venUsername and opt.start >= :start")
    public List<VenOpt> findScheduledOptAfter(@Param("venUsername") String venUsername, @Param("start") Long start);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven where opt.ven.username = :venUsername and opt.end < :end")
    public List<VenOpt> findScheduledOptBefore(@Param("venUsername") String venUsername, @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven where opt.ven.username = :venUsername")
    public List<VenOpt> findScheduledOpt(@Param("venUsername") String venUsername);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName) and opt.start >= :start and opt.end < :end")
    public List<VenOpt> findScheduledOptBetween(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName, @Param("start") Long start, @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName) and opt.start >= :start")
    public List<VenOpt> findScheduledOptAfter(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName, @Param("start") Long start);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName) and opt.end < :end")
    public List<VenOpt> findScheduledOptBefore(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName, @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName)")
    public List<VenOpt> findScheduledOpt(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource where opt.ven.username = :venUsername and resource.name = :resourceName and opt.start >= :start and opt.end < :end")
    public List<VenOpt> findResourceScheduledOptBetween(@Param("venUsername") String venUsername,
            @Param("resourceName") String resourceName, @Param("start") Long start, @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource where opt.ven.username = :venUsername and resource.name = :resourceName and opt.start >= :start")
    public List<VenOpt> findResourceScheduledOptAfter(@Param("venUsername") String venUsername,
            @Param("resourceName") String resourceName, @Param("start") Long start);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource where opt.ven.username = :venUsername and resource.name = :resourceName and opt.end < :end")
    public List<VenOpt> findResourceScheduledOptBefore(@Param("venUsername") String venUsername,
            @Param("resourceName") String resourceName, @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource where opt.ven.username = :venUsername and resource.name = :resourceName")
    public List<VenOpt> findResourceScheduledOpt(@Param("venUsername") String venUsername,
            @Param("resourceName") String resourceName);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName) and resource.name = :resourceName and opt.start >= :start and opt.end < :end")
    public List<VenOpt> findResourceScheduledOptBetween(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName, @Param("resourceName") String resourceName,
            @Param("start") Long start, @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName) and resource.name = :resourceName and opt.start >= :start")
    public List<VenOpt> findResourceScheduledOptAfter(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName, @Param("resourceName") String resourceName,
            @Param("start") Long start);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName) and resource.name = :resourceName and opt.end < :end")
    public List<VenOpt> findResourceScheduledOptBefore(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName, @Param("resourceName") String resourceName,
            @Param("end") Long end);

    @Query(value = "select opt from VenOpt opt inner join opt.ven ven inner join opt.venResource resource left join opt.marketContext marketContext where opt.ven.username = :venUsername and (opt.marketContext is null or opt.marketContext.name = :marketContextName) and resource.name = :resourceName")
    public List<VenOpt> findResourceScheduledOpt(@Param("venUsername") String venUsername,
            @Param("marketContextName") String marketContextName, @Param("resourceName") String resourceName);

    // 이 인터페이스는 클래스 레벨이 readOnly = true 다.
    // H2 는 읽기 전용 트랜잭션에서도 삭제를 그냥 처리했지만 PostgreSQL 은 거부한다.
    // 이 메서드만 쓰기 트랜잭션으로 덮어쓴다
    @Transactional
    @Modifying
    @Query("delete from VenOpt opt where opt.ven = :ven and opt.optId = :optId")
    public void deleteByVenAndoptId(@Param("ven") Ven ven, @Param("optId") String optId);

    // VEN 삭제 시 뒷정리용. venopt.ven_id 가 VEN 을 붙들고 있다
    @Transactional
    @Modifying
    @Query("delete from VenOpt opt where opt.ven = :ven")
    public void deleteByVen(@Param("ven") Ven ven);

}
