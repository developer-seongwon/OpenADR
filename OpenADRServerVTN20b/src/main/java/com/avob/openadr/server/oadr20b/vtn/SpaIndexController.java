package com.avob.openadr.server.oadr20b.vtn;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * React 앱의 진입점을 루트 경로에서 직접 내보낸다.
 *
 * 주소를 바꾸지 않는 게 핵심이다. 라우터의 basename 이 /testvtn/ 이라
 * 브라우저 주소가 /testvtn/ 그대로여야 홈 라우트에 매칭된다.
 * /testvtn/index.html 로 리다이렉트하면 남는 경로가 "index.html" 이 되어
 * 앱이 자기 404 화면을 띄운다.
 *
 * 포워드 대신 파일을 그대로 돌려주는 이유는, 포워드는 뷰 리졸버와 디스패치를 한 번 더
 * 타면서 에러 경로로 새기 쉬운데 이 방식은 그럴 여지가 없어서다.
 */
// REST API 가 아니라 화면을 내보내는 컨트롤러다. API 문서에는 넣지 않는다.
// 안 그러면 SPA 경로 7개가 전부 indexUsingGET 이라는 같은 이름으로 문서에 올라온다.
@Hidden
@RestController
public class SpaIndexController {

	/**
	 * SPA 가 쓰는 프론트 라우트들. 이 주소로 새로고침하거나 북마크로 바로 들어와도
	 * 서버가 404 를 내지 않고 index.html 을 돌려줘서 라우터가 이어받게 한다.
	 *
	 * REST API 경로(/Ven, /Account, /DemandResponseEvent ...)는 전부 대문자로 시작해서
	 * 소문자인 아래 경로들과 겹치지 않는다.
	 */
	@GetMapping(value = { "/", "/login", "/about", "/account/**", "/vtn_configuration/**", "/ven/**",
			"/event/**" }, produces = MediaType.TEXT_HTML_VALUE)
	public Resource index() {
		return new ClassPathResource("public/index.html");
	}

}
