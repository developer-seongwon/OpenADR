package com.avob.openadr.server.common.vtn.broker.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RabbitMQ rabbit_auth_backend_http 용 인증 엔드포인트. 요청을 전혀 검사하지 않고 모두 허용한다
 * (user 는 아이디/비밀번호와 상관없이 administrator 로 통과).
 *
 * 지금은 쓰지 않는다. RabbitMQ 는 내부 계정만으로 인증하고(docker/rabbitmq/rabbitmq.config),
 * VTN, DummyDRProgram 도 내부 계정으로 붙는다. RabbitMQ 설정에 http 백엔드를 다시 넣으면
 * 아무 계정으로나 브로커 관리자가 되므로, 쓰려면 실제 검사부터 구현해야 한다.
 * HttpSecurityConfig 의 POST .../auth/... permitAll 도 이 컨트롤러 때문에 있으니 지울 때 같이 지운다.
 */
@Deprecated
@Profile({ "!test", "external"})
@RestController
@RequestMapping("/auth")
public class RabbitmqHTTPAuthController {

//	[
//	  {rabbit, [{auth_backends, [rabbit_auth_backend_http]}]},
//	  {rabbitmq_auth_backend_http,
//	   [{http_method,   post},
//	    {user_path,     "https://vtn.oadr.com:8181/testvtn/auth/user"},
//	    {vhost_path,    "https://vtn.oadr.com:8181/testvtn/auth/vhost"},
//	    {resource_path, "https://vtn.oadr.com:8181/testvtn/auth/resource"},
//	    {topic_path,    "https://vtn.oadr.com:8181/testvtn/auth/topic"}]}
//	].

	private static final Logger LOGGER = LoggerFactory.getLogger(RabbitmqHTTPAuthController.class);

	@PostMapping("user")
	public String user(@RequestParam("username") String username, @RequestParam("password") String password) {
		LOGGER.info("Trying to authenticate user {}", username);
		return "allow administrator management";
	}

	@PostMapping("vhost")
	public String vhost(VirtualHostCheck check) {
		LOGGER.info("Checking vhost access with {}", check);
		return "allow";
	}

	@PostMapping("resource")
	public String resource(ResourceCheck check) {
		LOGGER.info("Checking resource access with {}", check);
		return "allow";
	}

	@PostMapping("topic")
	public String topic(TopicCheck check) {
		LOGGER.info("Checking topic access with {}", check);
		return "allow";
	}

}
