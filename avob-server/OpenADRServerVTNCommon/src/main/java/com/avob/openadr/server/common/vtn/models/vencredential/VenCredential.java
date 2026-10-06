package com.avob.openadr.server.common.vtn.models.vencredential;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * VTN 이 만들어 준 VEN 인증서 묶음(tar: crt, key, ca, fingerprint).
 *
 * 예전에는 VEN 을 만들 때 한 번만 내려 주고 어디에도 남기지 않아서, 잃어버리면 VEN 을 지우고 다시 만들어야 했다.
 * 이제 이 테이블에 남겨서 VEN 상세에서 다시 받을 수 있게 한다(VenController /Ven/{venID}/credentials).
 * VEN 개인 키가 VTN DB 에 그대로 남는다. 테스트 VTN 이라 감수한다.
 *
 * VEN 행과는 ven_id 로만 잇고 외래키는 걸지 않는다. VEN 을 지울 때 VenService.delete 가 같이 지운다.
 * Ven 엔티티에 바로 넣지 않은 이유는, VEN 은 요청마다 읽히는데 그때마다 tar 까지 실려 오지 않게 하려는 것이다
 */
@Entity
@Table(name = "ven_credential")
public class VenCredential implements Serializable {

	private static final long serialVersionUID = 4193251094872364121L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** Ven 행의 id(abstract_user.id). VEN 하나에 하나 */
	@Column(name = "ven_id", unique = true, nullable = false)
	private Long venId;

	/** 내려받을 때 쓰는 파일 이름(<Common Name>-credentials.tar) */
	@Column(name = "file_name")
	private String fileName;

	/** tar 내용. PostgreSQL 은 bytea, H2 는 varbinary 가 된다. tar 는 10KB 남짓이다 */
	@Column(name = "data", length = 1048576, nullable = false)
	private byte[] data;

	/** 만든 시각(epoch ms). 다시 만들면 바뀐다 */
	@Column(name = "created_datetime")
	private Long createdDatetime;

	public Long getId() {
		return id;
	}

	public Long getVenId() {
		return venId;
	}

	public void setVenId(Long venId) {
		this.venId = venId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public Long getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Long createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

}
