package com.avob.openadr.server.common.vtn;

import jakarta.annotation.Resource;
import jakarta.jms.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

@Profile("external")
@Configuration
public class VtnConfigRabbitmqBroker {

	@Resource
	private VtnConfig vtnConfig;

	@Bean
	public ConnectionFactory externalConnectionFactory() {
		RMQConnectionFactory rmqConnectionFactory = new RMQConnectionFactory();
		rmqConnectionFactory.setHost(vtnConfig.getBrokerHost());
		rmqConnectionFactory.setPort(vtnConfig.getBrokerPort());
		rmqConnectionFactory.setUsername(vtnConfig.getBrokerUser());
		rmqConnectionFactory.setPassword(vtnConfig.getBrokerPass());
		return rmqConnectionFactory;
	}

	@Bean
	public JmsTemplate externalJmsTemplate() throws Exception {
		return new JmsTemplate(externalConnectionFactory());
	}

	/**
	 * @JmsListener 가 쓰는 리스너 컨테이너 팩토리.
	 *
	 * 예전에는 부트의 JMS 자동설정이 이 빈을 만들어 줘서 여기에 없어도 됐다.
	 * 부트 4 가 JMS 자동설정을 spring-boot-jms 라는 별도 모듈로 빼면서, 그 모듈을
	 * 끌고 오는 JMS 스타터를 쓰지 않는 이 프로젝트에서는 자동설정이 사라졌다.
	 * 그 결과 external 프로파일로 띄우면 "No bean named 'jmsListenerContainerFactory'"
	 * 로 컨텍스트가 안 떴다. test/standalone 프로파일(VtnConfigEmbeddedBroker)은
	 * 원래부터 직접 정의하고 있었다. 이쪽도 같은 모양으로 맞춘다.
	 */
	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(externalConnectionFactory());
		return factory;
	}

}
