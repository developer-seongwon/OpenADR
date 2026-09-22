package com.avob.openadr.server.common.vtn.service.dtomapper;

import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.models.user.AbstractUser;
import com.avob.openadr.server.common.vtn.models.user.AbstractUserDto;
import com.avob.openadr.server.common.vtn.models.user.AbstractUserWithRoleDto;
import com.avob.openadr.server.common.vtn.models.user.OadrApp;
import com.avob.openadr.server.common.vtn.models.user.OadrAppDto;
import com.avob.openadr.server.common.vtn.models.user.OadrUser;
import com.avob.openadr.server.common.vtn.models.user.OadrUserDto;

/**
 * 사용자 계열 엔티티를 DTO 로 옮긴다.
 *
 * 예전에는 dozer 가 이름이 같은 필드를 리플렉션으로 찾아 복사했다.
 * dozer 6.5.2 가 jakarta 로 넘어오지 못해서 손으로 쓰는 매퍼로 바꿨다.
 *
 * 주의할 점이 두 가지 있다.
 * 하나는 id 다. 엔티티는 Long 인데 DTO 는 String 이라 dozer 가 알아서 바꿔 주던 것을
 * 여기서는 String.valueOf 로 직접 바꾼다. null 이면 그대로 null 이다.
 * 다른 하나는 password 와 needCertificateGeneration 이다.
 * 엔티티에 같은 이름의 필드가 없어서 dozer 도 채우지 못했다.
 * 비밀번호를 응답에 실어 보내지 않으려는 의도로 보여서 그대로 비워 둔다.
 */
@Service
public class UserDtoMapper {

	/**
	 * AbstractUserCreateDto 와 AbstractUserDto 가 가진 공통 필드를 채운다.
	 * 하위 DTO 들이 전부 이걸 상속하므로 여기 한 번만 쓴다.
	 */
	void copyCommon(AbstractUser src, AbstractUserDto dst) {
		dst.setId(src.getId() == null ? null : String.valueOf(src.getId()));
		dst.setUsername(src.getUsername());
		dst.setCommonName(src.getCommonName());
		dst.setAuthenticationType(src.getAuthenticationType());
	}

	public AbstractUserWithRoleDto toAbstractUserWithRoleDto(AbstractUser src) {
		if (src == null) {
			return null;
		}
		AbstractUserWithRoleDto dst = new AbstractUserWithRoleDto();
		copyCommon(src, dst);
		dst.setRoles(src.getRoles());
		return dst;
	}

	public OadrUserDto toOadrUserDto(OadrUser src) {
		if (src == null) {
			return null;
		}
		OadrUserDto dst = new OadrUserDto();
		copyCommon(src, dst);
		dst.setRoles(src.getRoles());
		dst.setEmail(src.getEmail());
		return dst;
	}

	public OadrAppDto toOadrAppDto(OadrApp src) {
		if (src == null) {
			return null;
		}
		OadrAppDto dst = new OadrAppDto();
		copyCommon(src, dst);
		dst.setRoles(src.getRoles());
		return dst;
	}

}
