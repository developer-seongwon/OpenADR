package com.avob.openadr.server.oadr20b.vtn;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * VTN20bSecurityApplicationTest 컨텍스트를 쓰는 테스트들의 공통 상위 클래스.
 *
 * 하는 일은 JmsTemplate 을 목으로 갈아 끼우는 것 하나다. VenDistributeService 와
 * DemandResponseEventPublisher 가 큐로 보내는 것을 막고, 목에 대고 무엇이
 * 나갔는지 확인하는 테스트가 여럿 있다.
 *
 * 왜 여기 있냐면, 예전에는 VTN20bSecurityApplicationTest 에 @MockBean 필드로
 * 있었는데 그게 Boot 4 에서 제거되기 때문이다. 후속인 @MockitoBean 은 테스트
 * 클래스와 그 상위 클래스에서만 처리되고 @Configuration 클래스에 붙이면 무시된다.
 * 평범한 @Bean 으로 두면 VtnConfigEmbeddedBroker 가 정의한 같은 이름의 빈과
 * 부딪힌다. 그래서 목의 자리를 설정 클래스에서 테스트 상위 클래스로 옮겼다.
 *
 * 컨텍스트 캐시 키에 bean override 가 들어가므로 이 컨텍스트를 쓰는 테스트는
 * 전부 이 클래스를 상속해야 한다. 일부만 상속하면 컨텍스트가 둘로 갈라진다.
 */
public abstract class AbstractVtn20bTest {

	@MockitoBean
	protected JmsTemplate jmsTemplate;

}
