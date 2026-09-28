package com.avob.openadr.client.http.oadr20a;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.avob.openadr.client.http.OadrHttpClient;
import com.avob.openadr.model.oadr20a.Oadr20aJAXBContext;
import com.avob.openadr.model.oadr20a.Oadr20aUrlPath;
import com.avob.openadr.model.oadr20a.builders.Oadr20aBuilders;
import com.avob.openadr.model.oadr20a.ei.EiActivePeriodType;
import com.avob.openadr.model.oadr20a.ei.EiEventSignalType;
import com.avob.openadr.model.oadr20a.ei.EiTargetType;
import com.avob.openadr.model.oadr20a.ei.EventDescriptorType;
import com.avob.openadr.model.oadr20a.ei.EventStatusEnumeratedType;
import com.avob.openadr.model.oadr20a.ei.SignalTypeEnumeratedType;
import com.avob.openadr.model.oadr20a.exception.Oadr20aException;
import com.avob.openadr.model.oadr20a.exception.Oadr20aHttpLayerException;
import com.avob.openadr.model.oadr20a.exception.Oadr20aMarshalException;
import com.avob.openadr.model.oadr20a.oadr.OadrDistributeEvent;
import com.avob.openadr.model.oadr20a.oadr.OadrDistributeEvent.OadrEvent;
import com.avob.openadr.model.oadr20a.oadr.OadrResponse;

public class OadrHttpClient20aTest {

	public static final String XSD_OADR20A_SCHEMA = "src/test/resources/oadr20a_schema";

	private Oadr20aJAXBContext jaxbContext;

	public OadrHttpClient20aTest() throws JAXBException {
		jaxbContext = Oadr20aJAXBContext.getInstance(XSD_OADR20A_SCHEMA);
	}

	private OadrDistributeEvent createOadrDistributeEvent() {
		long timestampStart = 0L;
		String eventXmlDuration = "PT1H";
		String toleranceXmlDuration = "PT5M";
		String notificationXmlDuration = "P1D";
		EiActivePeriodType eiActivePeriod = Oadr20aBuilders.newOadr20aEiActivePeriodTypeBuilder(timestampStart,
				eventXmlDuration, toleranceXmlDuration, notificationXmlDuration).build();

		String signalId = "0";
		String signalName = "simple";
		SignalTypeEnumeratedType signalType = SignalTypeEnumeratedType.LEVEL;
		String xmlDuration = "PT1H";
		float currentValue = 0;
		String intervalId = "intervalId";

		EiEventSignalType eiEventSignalType = Oadr20aBuilders
				.newOadr20aEiEventSignalTypeBuilder(signalId, signalName, signalType, currentValue)
				.addInterval(
						Oadr20aBuilders.newOadr20aIntervalTypeBuilder(intervalId, xmlDuration, currentValue).build())
				.build();

		String venId = "ven1";
		EiTargetType eiTarget = Oadr20aBuilders.newOadr20aEiTargetTypeBuilder().addVenId(venId).build();

		Long createdTimespamp = 0L;
		String eventId = "0";
		long modificationNumber = 0L;
		String marketContext = "";
		EventStatusEnumeratedType status = EventStatusEnumeratedType.ACTIVE;
		EventDescriptorType eventDescriptor = Oadr20aBuilders.newOadr20aEventDescriptorTypeBuilder(createdTimespamp,
				eventId, modificationNumber, marketContext, status).build();

		OadrEvent oadrEvent = Oadr20aBuilders.newOadr20aDistributeEventOadrEventBuilder()
				.withActivePeriod(eiActivePeriod).addEiEventSignal(eiEventSignalType).withEiTarget(eiTarget)
				.withEventDescriptor(eventDescriptor).withResponseRequired(false).build();

		return Oadr20aBuilders.newOadr20aDistributeEventBuilder("", "").addOadrEvent(oadrEvent).build();
	}

	// 예전에는 Apache 의 BasicHttpResponse 를 직접 만들었다.
	// 자바 표준 HttpResponse 는 인터페이스라서 상태 코드와 본문만 돌려주는 목으로 만든다
	@SuppressWarnings("unchecked")
	private HttpResponse<String> createHttpResponse(int responseCode, String payload) {
		HttpResponse<String> response = Mockito.mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(responseCode);
		when(response.body()).thenReturn(payload);
		return response;
	}

	@Test
	public void validPostTest() throws IOException, JAXBException, Oadr20aException,
			Oadr20aMarshalException, URISyntaxException, Oadr20aHttpLayerException {

		OadrHttpClient oadrHttpClient = Mockito.mock(OadrHttpClient.class);
		int scOk = HttpURLConnection.HTTP_OK;

		OadrResponse mockOadrResponse = Oadr20aBuilders.newOadr20aResponseBuilder("", scOk).build();
		String marshal = jaxbContext.marshal(mockOadrResponse);

		HttpResponse<String> response = this.createHttpResponse(scOk, marshal);
		when(oadrHttpClient.post(any(), any(), any())).thenReturn(response);

		OadrHttpClient20a client = new OadrHttpClient20a(oadrHttpClient);

		OadrDistributeEvent mockDistributeEvent = this.createOadrDistributeEvent();
		OadrResponse post = client.post(mockDistributeEvent,
				Oadr20aUrlPath.OADR_BASE_PATH + Oadr20aUrlPath.EI_EVENT_SERVICE, OadrResponse.class);

		assertEquals(String.valueOf(scOk), post.getEiResponse().getResponseCode());
	}

	@Test
	public void httpLayerErrorPostTest() throws IOException, JAXBException, Oadr20aException,
			Oadr20aMarshalException, URISyntaxException, Oadr20aHttpLayerException {

		// HTTP layer error
		OadrHttpClient oadrHttpClient = Mockito.mock(OadrHttpClient.class);
		int scForbidden = HttpURLConnection.HTTP_FORBIDDEN;

		OadrResponse mockOadrResponse = Oadr20aBuilders.newOadr20aResponseBuilder("", scForbidden).build();
		String marshal = jaxbContext.marshal(mockOadrResponse);

		HttpResponse<String> response = this.createHttpResponse(scForbidden, marshal);
		when(oadrHttpClient.post(any(), any(), any())).thenReturn(response);

		OadrHttpClient20a client = new OadrHttpClient20a(oadrHttpClient);

		OadrDistributeEvent mockDistributeEvent = this.createOadrDistributeEvent();

		boolean exception = false;
		try {
			client.post(mockDistributeEvent, Oadr20aUrlPath.OADR_BASE_PATH + Oadr20aUrlPath.EI_EVENT_SERVICE,
					OadrResponse.class);
		} catch (Oadr20aHttpLayerException e) {
			exception = true;
		}
		assertTrue(exception);

	}

	@Test
	public void requestMarshallingErrorPostTest() throws IOException, JAXBException,
			Oadr20aException, Oadr20aMarshalException, URISyntaxException, Oadr20aHttpLayerException {

		OadrHttpClient oadrHttpClient = Mockito.mock(OadrHttpClient.class);
		int scOk = HttpURLConnection.HTTP_OK;

		OadrResponse mockOadrResponse = Oadr20aBuilders.newOadr20aResponseBuilder("", scOk).build();
		String marshal = jaxbContext.marshal(mockOadrResponse);

		HttpResponse<String> response = this.createHttpResponse(scOk, marshal);
		when(oadrHttpClient.post(any(), any(), any())).thenReturn(response);

		OadrHttpClient20a client = new OadrHttpClient20a(oadrHttpClient);

		OadrDistributeEvent mockDistributeEvent = this.createOadrDistributeEvent();
		mockDistributeEvent.setVtnID(null);

		boolean exception = false;
		try {
			client.post(mockDistributeEvent, Oadr20aUrlPath.OADR_BASE_PATH + Oadr20aUrlPath.EI_EVENT_SERVICE,
					OadrResponse.class);
		} catch (Oadr20aException e) {
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	public void responseUnmarshallingErrorPostTest() throws IOException, JAXBException,
			Oadr20aException, Oadr20aMarshalException, URISyntaxException, Oadr20aHttpLayerException {

		OadrHttpClient oadrHttpClient = Mockito.mock(OadrHttpClient.class);
		int scOk = HttpURLConnection.HTTP_OK;

		String marshal = "";

		HttpResponse<String> response = this.createHttpResponse(scOk, marshal);
		when(oadrHttpClient.post(any(), any(), any())).thenReturn(response);

		OadrHttpClient20a client = new OadrHttpClient20a(oadrHttpClient);

		OadrDistributeEvent mockDistributeEvent = this.createOadrDistributeEvent();

		boolean exception = false;
		try {
			client.post(mockDistributeEvent, Oadr20aUrlPath.OADR_BASE_PATH + Oadr20aUrlPath.EI_EVENT_SERVICE,
					OadrResponse.class);
		} catch (Oadr20aException e) {
			exception = true;
		}
		assertTrue(exception);
	}
}
