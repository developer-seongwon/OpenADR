package com.avob.openadr.server.common.vtn.security;

import java.util.ArrayList;

import jakarta.annotation.Resource;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.VtnConfig;
import com.avob.openadr.server.common.vtn.models.user.AbstractUser;
import com.avob.openadr.server.common.vtn.models.user.AbstractUserDao;
import com.avob.openadr.server.common.vtn.models.user.OadrApp;
import com.avob.openadr.server.common.vtn.models.user.OadrUser;
import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.google.common.collect.Lists;

/**
 * grant role for abstract user
 * 
 * @author bzanni
 *
 */
@Service
public class OadrSecurityRoleService {

	@Resource
	private VtnConfig vtnConfig;

	@Resource
	private AbstractUserDao abstractUserDao;

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/**
	 * DigestAuthenticationFilter 에 넘길 사용자 정보다. 비밀번호와 함께 권한도 같이 채운다.
	 *
	 * 예전에는 권한을 비워서 돌려줬다. 필터가 미인증 토큰만 만들어 두면
	 * Spring Security 5 의 AbstractSecurityInterceptor 가 AuthenticationManager 로
	 * 다시 인증하면서 DigestAuthenticationProvider 가 권한을 채워 줬기 때문이다.
	 * Security 6 의 AuthorizationFilter 는 그 재인증을 하지 않는다.
	 * 토큰이 미인증에 권한도 비어 있는 채로 넘어가 403 이 난다.
	 *
	 * 그래서 여기서 권한까지 채우고, 필터 쪽은 createAuthenticatedToken 을 켠다.
	 * 필터가 다이제스트 해시를 검증한 뒤에만 이 사용자로 컨텍스트를 세우므로
	 * 권한을 미리 담아 둔다고 해서 검증이 느슨해지지는 않는다.
	 */
	public User digestUserDetail(String username) {
		AbstractUser abstractUser = saveFindUser(username);
		return this.grantRole(abstractUser, abstractUser.getDigestPassword());
	}

	public User grantDigestRole(String username, String password) {
		AbstractUser abstractUser = saveFindUser(username);
		if (!abstractUser.getDigestPassword().equals(password)) {
			throw new BadCredentialsException("Bad credentials given for user: '" + username + "'");
		}
		return this.grantRole(abstractUser, abstractUser.getDigestPassword());
	}

	public User grantBasicRole(String username, CharSequence rawPassword) {
		AbstractUser abstractUser = saveFindUser(username);
		if (!encoder.matches(rawPassword, abstractUser.getBasicPassword())) {
			throw new BadCredentialsException("Bad credentials given for user: '" + username + "'");
		}
		return this.grantRole(abstractUser, abstractUser.getBasicPassword());
	}

	public User grantX509Role(String username) {

		if (username.equals(vtnConfig.getOadr20bFingerprint()) || username.equals(vtnConfig.getXmppOadr20bFingerprint())) {
			return new User(username, "", Lists.newArrayList(new SimpleGrantedAuthority("ROLE_VTN")));
		}
		return this.grantRole(saveFindUser(username), "");
	}

	private AbstractUser saveFindUser(String username) {
		AbstractUser abstractUser = abstractUserDao.findOneByUsername(username);
		if (abstractUser == null) {
			throw new UsernameNotFoundException("");
		}
		return abstractUser;
	}

	private User grantRole(AbstractUser abstractUser, String password) {
		if (abstractUser instanceof Ven) {
			ArrayList<SimpleGrantedAuthority> roles = Lists.newArrayList(new SimpleGrantedAuthority("ROLE_VEN"));
			Ven ven = (Ven) abstractUser;
			if (ven.getRegistrationId() != null) {
				roles.add(new SimpleGrantedAuthority("ROLE_REGISTERED"));
			}
			return new User(abstractUser.getUsername(), password, roles);
		} else if (abstractUser instanceof OadrUser) {
			OadrUser user = (OadrUser) abstractUser;
			ArrayList<SimpleGrantedAuthority> roles = Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER"));
			user.getRoles().forEach(role -> {
				roles.add(new SimpleGrantedAuthority(role));
			});
			return new User(abstractUser.getUsername(), password, roles);
		} else if (abstractUser instanceof OadrApp) {
			OadrApp app = (OadrApp) abstractUser;
			ArrayList<SimpleGrantedAuthority> roles = Lists.newArrayList(new SimpleGrantedAuthority("ROLE_APP"));
			app.getRoles().forEach(role -> {
				roles.add(new SimpleGrantedAuthority(role));
			});
			return new User(abstractUser.getUsername(), password, roles);
		}

		throw new UsernameNotFoundException("");
	}

}
