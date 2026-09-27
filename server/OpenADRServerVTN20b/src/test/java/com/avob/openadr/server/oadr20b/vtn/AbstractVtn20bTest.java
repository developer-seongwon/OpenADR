package com.avob.openadr.server.oadr20b.vtn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

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

	@Autowired
	private PlatformTransactionManager transactionManager;

	/**
	 * 목 JmsTemplate 에 걸린 큐 리스너를 운영에서처럼 트랜잭션 밖에서 돌린다.
	 *
	 * VTN 은 drevent, command 큐 메시지를 트랜잭션 커밋 뒤에 보낸다(AfterCommit).
	 * 테스트는 목 JmsTemplate 의 convertAndSend 에서 리스너를 바로 부르므로 리스너가 커밋 직후(afterCommit)에
	 * 같은 스레드에서 돈다. 이때는 끝난 트랜잭션이 아직 묶여 있어서 그대로 부르면 리스너 안의 DB 작업이
	 * 이미 커밋된 트랜잭션에 끼어들어 반영되지 않는다.
	 * NOT_SUPPORTED 로 그 트랜잭션을 잠시 떼어 내면, 운영의 리스너 스레드처럼 트랜잭션 없이 시작하고
	 * 안에서 부르는 서비스(@Transactional)는 저마다 새 트랜잭션을 연다.
	 */
	protected void runAsJmsListener(Runnable listener) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
		template.executeWithoutResult(status -> listener.run());
	}

}
