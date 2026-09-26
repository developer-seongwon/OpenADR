package com.avob.openadr.server.oadr20b.vtn.utils;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.avob.openadr.server.oadr20b.vtn.service.VenDistributeService;
import com.avob.openadr.server.oadr20b.vtn.service.push.Oadr20bPushListener;

@Service
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
