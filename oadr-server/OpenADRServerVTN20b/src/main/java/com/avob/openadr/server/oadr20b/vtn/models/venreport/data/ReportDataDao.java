package com.avob.openadr.server.oadr20b.vtn.models.venreport.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ReportDataDao<T extends ReportData> extends JpaRepository<T, Long> {

	public List<T> findByVenIdAndReportSpecifierId(String venId, String reportSpecifierId);

	public List<T> findByVenIdAndReportSpecifierIdAndRid(String venId, String reportSpecifierId, String rid);

	// VEN 삭제 시 뒷정리용.
	// 이 테이블들은 외래키가 아니라 username 문자열을 들고 있어서 삭제를 막지는 않지만,
	// 남겨 두면 같은 이름으로 VEN 을 다시 만들었을 때 옛 데이터가 섞여 보인다
	@Transactional(readOnly = false)
	public void deleteByVenId(String venId);
}
