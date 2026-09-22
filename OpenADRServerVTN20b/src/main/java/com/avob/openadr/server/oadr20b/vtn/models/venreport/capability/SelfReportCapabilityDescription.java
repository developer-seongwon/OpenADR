package com.avob.openadr.server.oadr20b.vtn.models.venreport.capability;

import org.hibernate.Length;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.avob.openadr.server.common.vtn.models.venmarketcontext.VenMarketContext;

@Entity
@Table(name = "selfreportcapabilitydescription")
public class SelfReportCapabilityDescription extends ReportCapabilityDescription {

	@NotNull
	@ManyToOne
	@JoinColumn(name = "selfreportcapability_id")
	private SelfReportCapability selfReportCapability;

	@ManyToOne
	@JoinColumn(name = "marketcontext_id")
	private VenMarketContext venMarketContext;

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
    private String payload;

	public SelfReportCapability getSelfReportCapability() {
		return selfReportCapability;
	}

	public void setSelfReportCapability(SelfReportCapability selfReportCapability) {
		this.selfReportCapability = selfReportCapability;
	}

	public VenMarketContext getVenMarketContext() {
		return venMarketContext;
	}

	public void setVenMarketContext(VenMarketContext venMarketContext) {
		this.venMarketContext = venMarketContext;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

}
