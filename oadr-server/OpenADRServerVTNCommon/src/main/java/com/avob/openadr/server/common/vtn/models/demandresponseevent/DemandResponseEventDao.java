package com.avob.openadr.server.common.vtn.models.demandresponseevent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Demand Response Event model database interface
 *
 * Spring Data 3.0 부터 PagingAndSortingRepository 가 CrudRepository 를 더 이상
 * 상속하지 않아 save, findById, delete, count 가 사라졌다.
 * 둘을 모두 포함하는 JpaRepository 를 쓴다.
 *
 * @author bertrand
 *
 */
public interface DemandResponseEventDao
		extends JpaRepository<DemandResponseEvent, Long>, JpaSpecificationExecutor<DemandResponseEvent> {

	public void deleteByIdIn(Iterable<Long> entities);

}
