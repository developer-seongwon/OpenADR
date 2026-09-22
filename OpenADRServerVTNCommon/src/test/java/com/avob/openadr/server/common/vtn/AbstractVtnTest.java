package com.avob.openadr.server.common.vtn;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * ApplicationTest 컨텍스트를 쓰는 테스트들의 공통 상위 클래스.
 *
 * 하는 일은 JmsTemplate 을 목으로 갈아 끼우는 것 하나다. 테스트에서 실제로 큐에
 * 밀어넣지 않으려는 것이고, 목에 대고 검증하는 테스트도 있다.
 *
 * 왜 여기 있냐면, 예전에는 ApplicationTest 에 @MockBean 필드로 있었는데 그게
 * Boot 4 에서 제거되기 때문이다. 후속인 @MockitoBean 은 테스트 클래스와 그
 * 상위 클래스에서만 처리되고 @Configuration 클래스에 붙이면 무시된다.
 * 그래서 목의 자리를 설정 클래스에서 테스트 상위 클래스로 옮겼다.
 *
 * 컨텍스트 캐시 키에 bean override 가 들어가므로 이 컨텍스트를 쓰는 테스트는
 * 전부 이 클래스를 상속해야 한다. 일부만 상속하면 컨텍스트가 둘로 갈라진다.
 */
public abstract class AbstractVtnTest {

	@MockitoBean
	protected JmsTemplate jmsTemplate;

}
