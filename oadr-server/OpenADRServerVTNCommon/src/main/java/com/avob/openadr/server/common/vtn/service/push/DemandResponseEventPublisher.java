package com.avob.openadr.server.common.vtn.service.push;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.models.ven.Ven;

@Service
public class DemandResponseEventPublisher {

	public static final String OADR20A_QUEUE = "queue.drevent.oadr20a";

	public static final String OADR20B_QUEUE = "queue.drevent.queue.oadr20b";

	@Autowired
	private JmsTemplate jmsTemplate;

	// VEN 이름만 큐로 보내고, 받는 리스너가 그 VEN 에 보낼 이벤트를 DB 에서 조회한다.
	// 이벤트를 저장한 트랜잭션이 커밋되기 전에 리스너가 조회하면 새 이벤트를 못 보고 넘어가므로
	// 커밋 뒤에 보낸다(AfterCommit 참고)

	public void publish20a(Ven ven) {
		if (ven.getRegistrationId() != null) {
			String username = ven.getUsername();
			AfterCommit.run("publish 2.0a event notification",
					() -> jmsTemplate.convertAndSend(OADR20A_QUEUE, username));
		}
	}

	public void publish20b(Ven ven) {
		if (ven.getRegistrationId() != null) {
			String username = ven.getUsername();
			AfterCommit.run("publish 2.0b event notification",
					() -> jmsTemplate.convertAndSend(DemandResponseEventPublisher.OADR20B_QUEUE, username));
		}
	}

}
