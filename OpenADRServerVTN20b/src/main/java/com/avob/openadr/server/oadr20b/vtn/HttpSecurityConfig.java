package com.avob.openadr.server.oadr20b.vtn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.avob.openadr.server.common.vtn.security.BasicAuthenticationManager;
import com.avob.openadr.server.common.vtn.security.DigestAuthenticationProvider;
import com.avob.openadr.server.common.vtn.security.DigestUserDetailsService;

/**
 * Spring security configuration
 *
 * Spring Security 6 에서 WebSecurityConfigurerAdapter 가 제거돼
 * SecurityFilterChain 빈 방식으로 옮겼다. WebSecurity 쪽 ignoring 도
 * WebSecurityCustomizer 빈으로 바뀌었다.
 * EnableGlobalMethodSecurity 는 EnableMethodSecurity 로 대체됐다
 * (prePostEnabled 는 기본값이 true 다).
 *
 * authorizeRequests 와 regexMatchers 도 없어져서
 * authorizeHttpRequests 와 RegexRequestMatcher 로 옮겼다.
 *
 * @author bertrand
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class HttpSecurityConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpSecurityConfig.class);

	@Value("${vtn.cors:@null}")
	private String corsStr;

	private List<String> cors = null;

	@Resource
	private Oadr20bX509AuthenticatedUserDetailsService oadr20bX509AuthenticatedUserDetailsService;

	@Resource
	private BasicAuthenticationManager basicAuthenticationManager;

	@Resource
	private DigestUserDetailsService digestUserDetailsService;

	@Resource
	private DigestAuthenticationProvider digestAuthenticationProvider;

	@PostConstruct
	public void init() {
		if (corsStr != null) {
			cors = Arrays.asList(corsStr.split(","));
		} else {
			cors = new ArrayList<>();
		}
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		DigestAuthenticationEntryPoint authenticationEntryPoint = new DigestAuthenticationEntryPoint();
		authenticationEntryPoint.setKey(DigestAuthenticationProvider.DIGEST_KEY);
		authenticationEntryPoint.setRealmName(digestAuthenticationProvider.getRealm());
		authenticationEntryPoint.setNonceValiditySeconds(300);

		DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
		digestAuthenticationFilter.setAuthenticationEntryPoint(authenticationEntryPoint);
		digestAuthenticationFilter.setUserDetailsService(digestUserDetailsService);
		digestAuthenticationFilter.setPasswordAlreadyEncoded(true);
		// Security 6 의 AuthorizationFilter 는 미인증 토큰을 다시 인증해 주지 않는다.
		// 이걸 켜야 필터가 다이제스트 검증 후 권한이 담긴 인증 토큰을 바로 만든다.
		digestAuthenticationFilter.setCreateAuthenticatedToken(true);
		digestAuthenticationFilter.afterPropertiesSet();

		BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
		basicAuthenticationEntryPoint.setRealmName(BasicAuthenticationManager.BASIC_REALM);

		BasicAuthenticationFilter basicAuthenticationFilter = new BasicAuthenticationFilter(basicAuthenticationManager);

		http.cors(Customizer.withDefaults());
		http.csrf(AbstractHttpConfigurer::disable);
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.authorizeHttpRequests(auth -> auth
				.requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.OPTIONS, ".*")).permitAll()
				.requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.POST, ".*/auth/.*")).permitAll()
				// React 앱의 정적 번들은 공개한다.
				//
				// 예전에는 여기까지 인증을 걸어 두는 바람에 인증서 없이는 index.html 조차
				// 403 이라 화면이 아예 안 떴다. 그런데 LoginPage 에는 아이디/비밀번호 폼이 있고,
				// 그 폼은 SPA 가 뜬 뒤 Account API 가 401/403 을 돌려줘야 나타난다.
				// 정적 파일이 막혀 있으면 그 폴백에 도달할 방법이 없다.
				// 번들 자체에는 비밀이 없고, 데이터를 주는 API 는 그대로 보호된다.
				.requestMatchers("/", "/index.html", "/favicon.ico", "/manifest.json",
						"/asset-manifest.json", "/service-worker.js", "/static/**",
						// SPA 라우트로 바로 들어오거나 새로고침한 경우에도 index.html 을 내줘야 한다
						"/login", "/about", "/account/**", "/vtn_configuration/**", "/ven/**",
						"/event/**")
				.permitAll()
				// API 스키마도 공개한다.
				//
				// 화면이 swagger-client 로 API 를 부르는데, 그 클라이언트를 만들려면
				// 앱이 뜨는 시점에 스키마부터 받아와야 한다. 즉 로그인 전에 필요하다.
				// 여기가 막혀 있으면 화면이 "Can't connect to VTN backend" 만 띄우고 멈춘다.
				// 스키마는 어떤 엔드포인트가 있는지를 알려줄 뿐이고, 실제 데이터를 주는
				// 엔드포인트는 그대로 인증을 요구한다.
				.requestMatchers("/v3/api-docs/**", "/v3/api-docs", "/swagger-ui/**", "/swagger-ui.html")
				.permitAll()
				.anyRequest().authenticated());

		http.x509(x509 -> x509.subjectPrincipalRegex("CN=(.*?)(?:,|$)")
				.authenticationUserDetailsService(oadr20bX509AuthenticatedUserDetailsService));

		http.addFilter(digestAuthenticationFilter).addFilter(basicAuthenticationFilter);

		// VEN 호출과 스웨거 경로는 401 로, 나머지는 403 으로 돌려준다.
		// 브라우저가 VEN 경로에서 인증 창을 띄우지 않게 하려는 기존 동작을 그대로 옮겼다.
		http.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
			String path = request.getServletPath();
			LOGGER.error(path, authException);
			if (path.contains("/Ven") || path.contains("swagger") || path.contains("swagger-resources")
					|| path.contains("v2") || path.contains("swagger-ui") || path.contains("v3")) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			}
		}));

		return http.build();
	}

	/**
	 * 헬스 체크 경로는 시큐리티 필터 체인을 아예 타지 않는다.
	 * 예전 configure(WebSecurity) 의 ignoring 과 같은 역할이다.
	 */
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (WebSecurity web) -> web.ignoring().requestMatchers(RegexRequestMatcher.regexMatcher("/health"));
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "HEAD", "DELETE"));
		configuration.setAllowedOrigins(cors);

		configuration.applyPermitDefaultValues();
		configuration.setExposedHeaders(Arrays.asList("X-total-count", "X-total-page"));
		configuration.setAllowedHeaders(Arrays.asList("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
