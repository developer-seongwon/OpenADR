package com.avob.openadr.server.common.vtn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 하는 일이 없는 클래스다.
 *
 * 애플리케이션이 뜨면 push 프로파일로 컨텍스트를 하나 더 만들었다가 바로 닫는다.
 * 만든 컨텍스트를 어디에 넘기지도, 필드에 들고 있지도 않아서 close 되는 순간
 * 그 안의 빈은 전부 사라진다. push 기능은 메인 컨텍스트의 AsyncConfig 가 담당한다.
 *
 * 지우면 되는데 컨텍스트 refresh 의 부수 효과에 기대는 데가 있을 수 있어
 * 표시만 해 둔다.
 */
@Deprecated
@Component
public class WebInitializer implements ApplicationListener<ApplicationReadyEvent> {

	@Value("${oadr.supportPush:#{false}}")
	private Boolean supportPush;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent arg0) {
		if (supportPush) {
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
			context.getEnvironment().setActiveProfiles(AsyncConfig.PUSH_PROFILE);
			context.refresh();
			((ConfigurableApplicationContext) context).close();
		}
	}

}
