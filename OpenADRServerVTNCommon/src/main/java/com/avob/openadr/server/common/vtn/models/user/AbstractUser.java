package com.avob.openadr.server.common.vtn.models.user;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "abstract_user")
public abstract class AbstractUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4987643291191994638L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@Column(name = "username", unique = true)
	private String username;

	@Column(name = "token_1")
	private String digestPassword;

	@Column(name = "token_2")
	private String basicPassword;

	private String commonName;

	private String authenticationType;

	// @LazyCollection(FALSE) 는 하이버네이트 6 에서 deprecated, 7 에서 제거된다.
	// @ElementCollection 의 기본이 LAZY 라 EAGER 를 직접 지정해 같은 동작을 유지한다
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> roles;

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public AbstractUser() {
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getDigestPassword() {
		return digestPassword;
	}

	public String getBasicPassword() {
		return basicPassword;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setDigestPassword(String password) {
		this.digestPassword = password;
	}

	public void setBasicPassword(String basicPassword) {
		this.basicPassword = basicPassword;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getAuthenticationType() {
		return authenticationType;
	}

	public void setAuthenticationType(String authenticationType) {
		this.authenticationType = authenticationType;
	}

}
