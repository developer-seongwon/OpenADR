package com.avob.openadr.server.oadr20b.ven;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.x509.SubjectX500PrincipalExtractor;

import com.avob.openadr.model.oadr20b.Oadr20bUrlPath;

/**
 * Spring security configuration
 *
 * Spring Security 6 에서 WebSecurityConfigurerAdapter 가 제거돼
 * SecurityFilterChain 빈 방식으로 옮겼다.
 * EnableGlobalMethodSecurity 는 EnableMethodSecurity 로 대체됐다
 * (prePostEnabled 는 기본값이 true 다).
 * authorizeRequests 와 antMatchers 도 authorizeHttpRequests 와 requestMatchers 로 바뀌었다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class HttpSecurityConfig {

	@Resource
	private Oadr20bX509AuthenticatedUserDetailsService oadr20bX509AuthenticatedUserDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(AbstractHttpConfigurer::disable);
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.authorizeHttpRequests(
				auth -> auth.requestMatchers(Oadr20bUrlPath.OADR_BASE_PATH + "/**").authenticated());

		// 예전에는 subjectPrincipalRegex("CN=(.*?)(?:,|$)") 였는데
		// 그건 Spring Security 6.5 에서 deprecated 됐다.
		// 정규식으로 DN 문자열을 긁던 SubjectDnX509PrincipalExtractor 도 같이 deprecated 다.
		// SubjectX500PrincipalExtractor 는 X500Principal 을 RDN 으로 제대로 파싱해서
		// CN 을 꺼낸다. 기본값이 CN 이라 따로 지정할 것도 없고, 결과는 같다
		SubjectX500PrincipalExtractor principalExtractor = new SubjectX500PrincipalExtractor();
		http.x509(x509 -> x509.x509PrincipalExtractor(principalExtractor)
				.authenticationUserDetailsService(oadr20bX509AuthenticatedUserDetailsService));

		return http.build();
	}

}
