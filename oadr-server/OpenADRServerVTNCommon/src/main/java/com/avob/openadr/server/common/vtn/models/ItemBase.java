package com.avob.openadr.server.common.vtn.models;

import org.hibernate.Length;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

@Embeddable
public class ItemBase {

	@Column(name = "item_description")
	private String itemDescription;

	@Column(name = "item_units")
	private String itemUnits;

	@Column(name = "si_scale_code")
	private String siScaleCode;

	@Column(name = "xml_type")
	private String xmlType;

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
	@Column(name = "attributes", length = Length.LONG32)
	private String attributes;

	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	public String getItemUnits() {
		return itemUnits;
	}

	public void setItemUnits(String itemUnits) {
		this.itemUnits = itemUnits;
	}

	public String getSiScaleCode() {
		return siScaleCode;
	}

	public void setSiScaleCode(String siScaleCode) {
		this.siScaleCode = siScaleCode;
	}

	public String getXmlType() {
		return xmlType;
	}

	public void setXmlType(String xmlType) {
		this.xmlType = xmlType;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

}
