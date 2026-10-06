package com.avob.openadr.server.common.vtn;

import static org.springframework.security.config.Customizer.withDefaults;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

import com.avob.openadr.server.common.vtn.security.BasicAuthenticationManager;
import com.avob.openadr.server.common.vtn.security.DigestAuthenticationProvider;
import com.avob.openadr.server.common.vtn.security.DigestUserDetailsService;

/**
 * Spring security configuration
 *
 * Spring Security 6 에서 WebSecurityConfigurerAdapter 가 제거돼
 * SecurityFilterChain 빈 방식으로 옮겼다.
 * EnableGlobalMethodSecurity 도 EnableMethodSecurity 로 대체됐다
 * (prePostEnabled 는 기본값이 true 다).
 *
 * @author bertrand
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class HttpSecurityConfig {

	@Resource
	private BasicAuthenticationManager basicAuthenticationManager;

	@Resource
	private DigestUserDetailsService digestUserDetailsService;

	@Resource
	private DigestAuthenticationProvider digestAuthenticationProvider;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		DigestAuthenticationEntryPoint authenticationEntryPoint = new DigestAuthenticationEntryPoint();
		authenticationEntryPoint.setKey(DigestAuthenticationProvider.DIGEST_KEY);
		authenticationEntryPoint.setRealmName(digestAuthenticationProvider.getRealm());

		DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
		digestAuthenticationFilter.setAuthenticationEntryPoint(authenticationEntryPoint);
		digestAuthenticationFilter.setUserDetailsService(digestUserDetailsService);
		digestAuthenticationFilter.setPasswordAlreadyEncoded(true);

		BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
		basicAuthenticationEntryPoint.setRealmName(BasicAuthenticationManager.BASIC_REALM);

		BasicAuthenticationFilter basicAuthenticationFilter = new BasicAuthenticationFilter(
				basicAuthenticationManager);

		http.csrf(AbstractHttpConfigurer::disable);
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.addFilter(digestAuthenticationFilter)
				.addFilter(basicAuthenticationFilter)
				.exceptionHandling(withDefaults())
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

		return http.build();
	}

}
