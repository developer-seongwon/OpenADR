package com.avob.openadr.server.common.vtn.models.demandresponseevent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandResponseEventSignalDao extends JpaRepository<DemandResponseEventSignal, Long> {

	// 저장 순서(id) 대로 돌려준다. oadrDistributeEvent 의 시그널 순서가 이 순서다.
	// 연계 규격은 기본 지령(SIG_01) 다음에 참여자, 자원별 시그널이 오는 순서를 쓴다.
	// 정렬이 없으면 DB 가 내주는 순서라 보장이 없었다
	public List<DemandResponseEventSignal> findByEventOrderByIdAsc(DemandResponseEvent event);

	@Modifying
	@Query
	public void deleteByEventIn(List<DemandResponseEvent> event);

	@Modifying
	public void deleteByEventId(@Param("eventId") Long eventId);

}
