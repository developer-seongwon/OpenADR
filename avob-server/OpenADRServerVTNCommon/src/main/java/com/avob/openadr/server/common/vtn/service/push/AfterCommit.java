package com.avob.openadr.server.common.vtn.service.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 큐나 토픽으로 보내는 일을 DB 트랜잭션 커밋 뒤로 미룬다.
 *
 * JMS 전송은 DB 트랜잭션에 묶이지 않는다. 트랜잭션 안에서 바로 보내면 받는 쪽(다른 스레드의 리스너,
 * 다른 프로세스의 앱)이 커밋 전에 메시지를 받아 방금 저장한 데이터를 못 찾을 수 있다.
 * 예를 들어 이벤트를 만들고 drevent 큐로 VEN 이름을 보내면 리스너가 그 VEN 에 보낼 이벤트를 조회하는데,
 * 커밋 전이면 새 이벤트가 안 보여서 VEN 에 안 나간다. 롤백되면 나가지 말아야 할 메시지가 나간다.
 *
 * 트랜잭션 밖(리스너 스레드, 스케줄러 등)에서 불리면 바로 보낸다.
 *
 * 주의: 커밋 뒤(afterCommit)에 도는 코드가 DB 를 쓰려면 새 트랜잭션(REQUIRES_NEW)이어야 한다.
 * 이 시점에는 끝난 트랜잭션의 자원이 아직 묶여 있어서 REQUIRED 로 들어가면 이미 커밋된 트랜잭션에
 * 끼어들고 쓴 내용이 반영되지 않는다. 운영에서는 여기서 JMS 로 보내기만 하므로 상관없다.
 * 테스트가 목 JmsTemplate 에서 리스너를 바로 부를 때는 새 트랜잭션으로 감싼다(AbstractVtn20bTest 참고).
 */
public final class AfterCommit {

	private static final Logger LOGGER = LoggerFactory.getLogger(AfterCommit.class);

	private AfterCommit() {
	}

	/**
	 * 트랜잭션 안이면 커밋 뒤에, 밖이면 바로 action 을 돌린다. 롤백되면 돌리지 않는다.
	 *
	 * @param what   실패했을 때 로그에 남길 설명
	 * @param action 보낼 일. 보낼 내용(직렬화 결과 등)은 부르기 전에 만들어 두는 게 좋다.
	 *               커밋 뒤에는 엔티티 상태가 바뀌었을 수 있다
	 */
	public static void run(String what, Runnable action) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			action.run();
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					action.run();
				} catch (RuntimeException e) {
					// 이미 커밋된 뒤라 던져도 되돌릴 게 없고, 던지면 호출한 쪽에 커밋 실패처럼 보인다. 로그만 남긴다
					LOGGER.error("Can't " + what + " after commit", e);
				}
			}
		});
	}

}
