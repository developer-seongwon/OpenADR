package com.avob.openadr.server.common.vtn.service.push;

import jakarta.annotation.Resource;
import jakarta.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.VtnConfig;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQTextMessage;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class OadrAppNotificationPublisher {

	private static final Logger LOGGER = LoggerFactory.getLogger(OadrAppNotificationPublisher.class);

	public static final String OADR_APP_NOTIFICATION_TOPIC = "topic.app.notification";

	@Autowired
	private JmsTemplate jmsTemplate;

	@Resource
	private VtnConfig vtnConfig;

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * 앱 알림을 보낸다. 트랜잭션 안에서 불리면 커밋이 끝난 뒤에 보낸다.
	 *
	 * 예를 들어 oadrRegisterReport 는 트랜잭션 안에서 리포트 명세를 저장하고 바로 이 알림을 보낸다.
	 * 알림을 받은 앱(DummyDRProgram)은 곧바로 그 명세로 구독 API 를 부르는데,
	 * 커밋 전이라 VTN 이 명세를 못 찾고 406(has no report capability)으로 거절했다.
	 * JMS 전송은 DB 트랜잭션에 묶이지 않으므로 커밋 뒤로 미룬다. 롤백되면 보내지 않는다.
	 * 직렬화는 지금 해 둔다. 커밋 뒤에는 엔티티 상태가 바뀌었을 수 있다.
	 */
	public void notify(Object payload, String subTopic, String venId) {
		String json;
		try {
			json = mapper.writeValueAsString(payload);
		} catch (JacksonException e) {
			LOGGER.error("Can't marshall message for notification", e);
			return;
		}

		// 커밋 뒤로 미루는 일은 AfterCommit 이 한다. drevent, command 큐도 같은 방식이다
		AfterCommit.run("publish notification", () -> send(json, subTopic, venId));
	}

	private void send(String writeValueAsString, String subTopic, String venId) {
		if (vtnConfig.hasExternalRabbitMQBroker()) {
			RMQDestination destination = new RMQDestination();
			destination.setDestinationName(OADR_APP_NOTIFICATION_TOPIC + "." + subTopic + ".*");
			destination.setAmqpExchangeName("jms.durable.queues");
			destination.setQueue(true);
			destination.setAmqpRoutingKey(OADR_APP_NOTIFICATION_TOPIC + "." + subTopic + ".*");
			RMQTextMessage msg = new RMQTextMessage();
			try {
				msg.setText(writeValueAsString);
				msg.setStringProperty("venID", venId);
				jmsTemplate.convertAndSend(destination, msg);
			} catch (JMSException e) {
				LOGGER.error("Can't publish notification", e);
			}
		} else {

			jmsTemplate.convertAndSend(OADR_APP_NOTIFICATION_TOPIC + "." + subTopic + "." + venId,
					writeValueAsString, new MessagePostProcessor() {

						@Override
						public jakarta.jms.Message postProcessMessage(jakarta.jms.Message arg0) throws JMSException {
							arg0.setStringProperty("venID", venId);
							return arg0;
						}

					});
		}

	}
}
