package com.avob.openadr.server.oadr20b.vtn;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.avob.openadr.server.oadr20b.vtn.converter.InstantConverter;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new InstantConverter());
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {

		// 루트("/")는 SpaIndexController 가 index.html 을 직접 내보낸다.
		// 여기서 리다이렉트하면 주소가 /testvtn/index.html 로 바뀌는데,
		// React 라우터의 basename 이 /testvtn/ 이라 남는 경로가 "index.html" 이 되고
		// 매칭되는 라우트가 없어서 앱이 자기 404 화면을 띄운다.

		// springfox 시절에는 /api/v2/api-docs 로 리다이렉트를 걸어 뒀는데
		// 지금은 프론트도 DummyDRProgram 도 /v3/api-docs 를 직접 부른다. 쓰는 데가 없어서 걷어냈다.
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// springfox 는 swagger-ui.html 과 webjars 를 직접 서빙해야 했다.
		// springdoc 은 /swagger-ui/** 를 자기가 처리하므로 그 핸들러들은 지웠다.
		// 남은 건 React 번들을 내보내는 것 하나다.
		registry.addResourceHandler("/**").addResourceLocations("classpath:/public/");

	}

}
