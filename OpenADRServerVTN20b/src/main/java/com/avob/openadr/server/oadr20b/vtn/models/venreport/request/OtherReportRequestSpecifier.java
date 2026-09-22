package com.avob.openadr.server.oadr20b.vtn.models.venreport.request;

import org.hibernate.Length;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescription;

@Entity
@Table(name = "otherreportrequestspecifier")
public class OtherReportRequestSpecifier {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "request_id")
	private OtherReportRequest request;

	@ManyToOne
	@JoinColumn(name = "otherreportcapabilitydescription_id")
	private OtherReportCapabilityDescription otherReportCapabilityDescription;

	private Boolean archived;

	private Long lastUpdateDatetime;

	@Lob
	// PostgreSQL 에서 @Lob String 은 oid, 즉 라지 오브젝트로 매핑된다.
	// 라지 오브젝트는 트랜잭션 안에서만 읽고 쓸 수 있어서
	// "Large Objects may not be used in auto-commit mode" 로 죽는다.
	// H2 는 CLOB 으로 처리해서 문제가 안 드러났다.
	// 길이 제한 없는 문자 컬럼(PostgreSQL 의 text)으로 가게 명시한다.
	//
	// LONGVARCHAR 만 주면 부족하다. Hibernate 6 는 길이를 안 알려주면
	// varchar(32600) 으로 DDL 을 만들고, 그걸 넘기는 값이 들어오면
	// "value too long for type character varying(32600)" 으로 insert 가 터진다.
	// 길이를 LONG32 로 줘야 text 가 된다
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(length = Length.LONG32)
	private String lastUpdateValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OtherReportRequest getRequest() {
		return request;
	}

	public void setRequest(OtherReportRequest request) {
		this.request = request;
	}

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public OtherReportCapabilityDescription getOtherReportCapabilityDescription() {
		return otherReportCapabilityDescription;
	}

	public void setOtherReportCapabilityDescription(OtherReportCapabilityDescription otherReportCapabilityDescription) {
		this.otherReportCapabilityDescription = otherReportCapabilityDescription;
	}

	public Long getLastUpdateDatetime() {
		return lastUpdateDatetime;
	}

	public void setLastUpdateDatetime(Long lastUpdateDatetime) {
		this.lastUpdateDatetime = lastUpdateDatetime;
	}

	public String getLastUpdateValue() {
		return lastUpdateValue;
	}

	public void setLastUpdateValue(String lastUpdateValue) {
		this.lastUpdateValue = lastUpdateValue;
	}
}
