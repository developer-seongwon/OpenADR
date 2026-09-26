package com.avob.openadr.server.oadr20b.vtn.utils;

import jakarta.annotation.Resource;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.avob.openadr.server.oadr20b.vtn.service.VenDistributeService;
import com.avob.openadr.server.oadr20b.vtn.service.push.Oadr20bPushListener;

/**
 * 쓰이지 않는다. 지워도 된다.
 *
 * VTN 서비스들은 VenDistributeService 를 @Resource 필드 이름(venDistributeService)으로 받아서
 * 이 빈(mockVenDistributeService)이 아니라 진짜 빈이 주입됐다. 테스트는 목 JmsTemplate 의
 * convertAndSend 에서 Oadr20bPushListener 를 직접 불러 같은 일을 한다.
 * 그래서 @Service 와 VTN20bSecurityApplicationTest 의 등록을 뗐다.
 */
@Deprecated
public class MockVenDistributeService extends VenDistributeService {

	@Resource
	private Oadr20bPushListener oadr20bPushListener;

	@Resource
	private PlatformTransactionManager transactionManager;

	// send 는 커밋 뒤(afterCommit)에 불린다. 리스너를 운영처럼 트랜잭션 밖에서 돌리려고
	// 끝난 트랜잭션을 NOT_SUPPORTED 로 떼어 낸다(AbstractVtn20bTest.runAsJmsListener 와 같은 이유)
	@Override
	protected void send(String command) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
		template.executeWithoutResult(status -> oadr20bPushListener.receiveCommand(command));
	}

}
