package com.avob.openadr.server.oadr20b.vtn.service;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.avob.openadr.model.oadr20b.Oadr20bJAXBContext;
import com.avob.openadr.model.oadr20b.exception.Oadr20bApplicationLayerException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bMarshalException;
import com.avob.openadr.model.oadr20b.oadr.OadrCancelPartyRegistrationType;
import com.avob.openadr.model.oadr20b.oadr.OadrCancelReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreateReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrDistributeEventType;
import com.avob.openadr.model.oadr20b.oadr.OadrRegisterReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrRequestReregistrationType;
import com.avob.openadr.model.oadr20b.oadr.OadrUpdateReportType;
import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.service.push.AfterCommit;
import com.avob.openadr.server.common.vtn.service.push.VenCommandDto;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class VenDistributeService {

	public static final String OADR20B_QUEUE = "queue.command.oadr20b";

	@Resource
	private Oadr20bJAXBContext jaxbContext;

	@Autowired
	private JmsTemplate jmsTemplate;

	private ObjectMapper mapper = new ObjectMapper();

	// Jackson 3 의 JacksonException 은 unchecked 라 throws 선언이 필요 없다.
	// 예전에는 throws JsonProcessingException 이었다
	// 명령은 지금 직렬화하고 보내는 건 트랜잭션 커밋 뒤로 미룬다(AfterCommit 참고).
	// 받는 리스너가 VEN 응답을 처리하면서 방금 저장한 리포트 요청 등을 조회하기 때문이다
	private <T> void publish(Ven ven, String payload, Class<T> klass) {
		VenCommandDto<T> command = new VenCommandDto<T>(ven, payload, klass);
		String json = mapper.writeValueAsString(command);
		AfterCommit.run("distribute command to " + ven.getUsername(), () -> this.send(json));
	}

	protected void send(String payload) {
		jmsTemplate.convertAndSend(OADR20B_QUEUE, payload);
	}

	public void distribute(Ven ven, Object payload) throws Oadr20bApplicationLayerException {
		try {
			String marshalRoot = jaxbContext.marshalRoot(payload);
			if (payload instanceof OadrDistributeEventType) {

				this.publish(ven, marshalRoot, OadrDistributeEventType.class);

			} else if (payload instanceof OadrCancelReportType) {

				this.publish(ven, marshalRoot, OadrCancelReportType.class);

			} else if (payload instanceof OadrCreateReportType) {

				this.publish(ven, marshalRoot, OadrCreateReportType.class);

			} else if (payload instanceof OadrRegisterReportType) {

				this.publish(ven, marshalRoot, OadrRegisterReportType.class);

			} else if (payload instanceof OadrUpdateReportType) {

				this.publish(ven, marshalRoot, OadrUpdateReportType.class);

			} else if (payload instanceof OadrCancelPartyRegistrationType) {

				this.publish(ven, marshalRoot, OadrCancelPartyRegistrationType.class);

			} else if (payload instanceof OadrRequestReregistrationType) {

				this.publish(ven, marshalRoot, OadrRequestReregistrationType.class);

			} else {
				throw new Oadr20bApplicationLayerException("Can't distribute an unknown payload type");
			}
		} catch (JacksonException | Oadr20bMarshalException e) {
			throw new Oadr20bApplicationLayerException(e);
		}
	}

}
