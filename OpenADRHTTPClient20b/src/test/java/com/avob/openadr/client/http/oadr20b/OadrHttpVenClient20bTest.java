package com.avob.openadr.client.http.oadr20b;

import static org.mockito.Mockito.when;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.avob.openadr.client.http.oadr20b.ven.OadrHttpVenClient20b;
import com.avob.openadr.model.oadr20b.builders.Oadr20bEiBuilders;
import com.avob.openadr.model.oadr20b.builders.Oadr20bEiEventBuilders;
import com.avob.openadr.model.oadr20b.builders.Oadr20bEiOptBuilders;
import com.avob.openadr.model.oadr20b.builders.Oadr20bEiRegisterPartyBuilders;
import com.avob.openadr.model.oadr20b.builders.Oadr20bEiReportBuilders;
import com.avob.openadr.model.oadr20b.builders.Oadr20bResponseBuilders;
import com.avob.openadr.model.oadr20b.ei.EiActivePeriodType;
import com.avob.openadr.model.oadr20b.ei.EiEventSignalType;
import com.avob.openadr.model.oadr20b.ei.EiTargetType;
import com.avob.openadr.model.oadr20b.ei.EventDescriptorType;
import com.avob.openadr.model.oadr20b.ei.EventStatusEnumeratedType;
import com.avob.openadr.model.oadr20b.ei.SignalNameEnumeratedType;
import com.avob.openadr.model.oadr20b.ei.SignalTypeEnumeratedType;
import com.avob.openadr.model.oadr20b.exception.Oadr20bException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bHttpLayerException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bXMLSignatureException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bXMLSignatureValidationException;
import com.avob.openadr.model.oadr20b.oadr.OadrCancelOptType;
import com.avob.openadr.model.oadr20b.oadr.OadrCancelPartyRegistrationType;
import com.avob.openadr.model.oadr20b.oadr.OadrCancelReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrCanceledOptType;
import com.avob.openadr.model.oadr20b.oadr.OadrCanceledPartyRegistrationType;
import com.avob.openadr.model.oadr20b.oadr.OadrCanceledReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreateOptType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreatePartyRegistrationType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreateReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreatedEventType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreatedOptType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreatedPartyRegistrationType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreatedReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrDistributeEventType;
import com.avob.openadr.model.oadr20b.oadr.OadrDistributeEventType.OadrEvent;
import com.avob.openadr.model.oadr20b.oadr.OadrPollType;
import com.avob.openadr.model.oadr20b.oadr.OadrQueryRegistrationType;
import com.avob.openadr.model.oadr20b.oadr.OadrRegisterReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrRegisteredReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrRequestEventType;
import com.avob.openadr.model.oadr20b.oadr.OadrResponseType;
import com.avob.openadr.model.oadr20b.oadr.OadrUpdateReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrUpdatedReportType;
import com.avob.openadr.security.exception.OadrSecurityException;

public class OadrHttpVenClient20bTest {

	@Test
	public void testOadrRequestEvent() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
			OadrSecurityException, JAXBException, URISyntaxException, Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		long timestampStart = 0L;
		String eventXmlDuration = "PT1H";
		String toleranceXmlDuration = "PT5M";
		String notificationXmlDuration = "P1D";
		EiActivePeriodType eiActivePeriod = Oadr20bEiEventBuilders.newOadr20bEiActivePeriodTypeBuilder(timestampStart,
				eventXmlDuration, toleranceXmlDuration, notificationXmlDuration).build();

		String signalId = "0";
		SignalNameEnumeratedType signalName = SignalNameEnumeratedType.SIMPLE;
		SignalTypeEnumeratedType signalType = SignalTypeEnumeratedType.LEVEL;
		float currentValue = 0;

		EiEventSignalType eiEventSignalType = Oadr20bEiEventBuilders
				.newOadr20bEiEventSignalTypeBuilder(signalId, signalName, signalType, currentValue).build();

		String venId = "ven1";
		EiTargetType eiTarget = Oadr20bEiBuilders.newOadr20bEiTargetTypeBuilder().addVenId(venId).build();

		Long createdTimespamp = 0L;
		String eventId = "0";
		long modificationNumber = 0L;
		String marketContext = "";
		EventStatusEnumeratedType status = EventStatusEnumeratedType.ACTIVE;
		EventDescriptorType eventDescriptor = Oadr20bEiEventBuilders.newOadr20bEventDescriptorTypeBuilder(
				createdTimespamp, eventId, modificationNumber, marketContext, status).build();

		OadrEvent oadrEvent = Oadr20bEiEventBuilders.newOadr20bDistributeEventOadrEventBuilder()
				.withActivePeriod(eiActivePeriod).addEiEventSignal(eiEventSignalType).withEiTarget(eiTarget)
				.withEventDescriptor(eventDescriptor).build();

		OadrDistributeEventType mockDistributeEvent = Oadr20bEiEventBuilders.newOadr20bDistributeEventBuilder("", "")
				.addOadrEvent(oadrEvent).build();

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrHttpVenClient20b oadrHttpVenClient20b = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrDistributeEventType, JAXBElement<OadrRequestEventType>>post(
				ArgumentMatchers.<JAXBElement<OadrRequestEventType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockDistributeEvent);

		OadrRequestEventType requestEvent = new OadrRequestEventType();
		oadrHttpVenClient20b.oadrRequestEvent(requestEvent);
	}

	@Test
	public void testOadrCreatedEventType() throws OadrSecurityException, JAXBException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, URISyntaxException, Oadr20bException,
			Oadr20bHttpLayerException, Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrResponseType mockOadrResponseType = Oadr20bResponseBuilders
				.newOadr20bResponseBuilder("", HttpURLConnection.HTTP_OK, "venId").build();

		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrResponseType, JAXBElement<OadrCreatedEventType>>post(
				ArgumentMatchers.<JAXBElement<OadrCreatedEventType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockOadrResponseType);

		OadrCreatedEventType createdEvent = new OadrCreatedEventType();
		oadrHttpVenClient20bTestClass.oadrCreatedEvent(createdEvent);

	}

	@Test
	public void oadrCreatedReportTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrResponseType mockOadrResponseType = Oadr20bResponseBuilders
				.newOadr20bResponseBuilder("", HttpURLConnection.HTTP_OK, "venId").build();

		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrResponseType, JAXBElement<OadrResponseType>>post(
				ArgumentMatchers.<JAXBElement<OadrResponseType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockOadrResponseType);

		OadrCreatedReportType payload = new OadrCreatedReportType();
		oadrHttpVenClient20bTestClass.oadrCreatedReport(payload);
	}

	@Test
	public void oadrCreateReportTest() throws Oadr20bException, Oadr20bHttpLayerException, Oadr20bXMLSignatureException,
			Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrCreatedReportType build = Oadr20bEiReportBuilders
				.newOadr20bCreatedReportBuilder("", HttpURLConnection.HTTP_OK, "venId").build();

		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrCreatedReportType, JAXBElement<OadrCreatedReportType>>post(
				ArgumentMatchers.<JAXBElement<OadrCreatedReportType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrCreateReportType payload = new OadrCreateReportType();
		oadrHttpVenClient20bTestClass.oadrCreateReport(payload);
	}

	@Test
	public void oadrUpdateReportTest() throws Oadr20bException, Oadr20bHttpLayerException, Oadr20bXMLSignatureException,
			Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrUpdatedReportType build = Oadr20bEiReportBuilders
				.newOadr20bUpdatedReportBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrUpdatedReportType, JAXBElement<OadrUpdatedReportType>>post(
				ArgumentMatchers.<JAXBElement<OadrUpdatedReportType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrUpdateReportType payload = new OadrUpdateReportType();
		oadrHttpVenClient20bTestClass.oadrUpdateReport(payload);
	}

	@Test
	public void oadrRegisterReportTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrRegisteredReportType build = Oadr20bEiReportBuilders
				.newOadr20bRegisteredReportBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrRegisteredReportType, JAXBElement<OadrRegisteredReportType>>post(
				ArgumentMatchers.<JAXBElement<OadrRegisteredReportType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrRegisterReportType payload = new OadrRegisterReportType();
		oadrHttpVenClient20bTestClass.oadrRegisterReport(payload);
	}

	@Test
	public void oadrUpdatedReportTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrResponseType mockOadrResponseType = Oadr20bResponseBuilders
				.newOadr20bResponseBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrResponseType, JAXBElement<OadrResponseType>>post(
				ArgumentMatchers.<JAXBElement<OadrResponseType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockOadrResponseType);

		OadrUpdatedReportType payload = new OadrUpdatedReportType();
		oadrHttpVenClient20bTestClass.oadrUpdatedReport(payload);
	}

	@Test
	public void oadrRegisteredReportTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrResponseType mockOadrResponseType = Oadr20bResponseBuilders
				.newOadr20bResponseBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrResponseType, JAXBElement<OadrResponseType>>post(
				ArgumentMatchers.<JAXBElement<OadrResponseType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockOadrResponseType);

		OadrRegisteredReportType payload = new OadrRegisteredReportType();
		oadrHttpVenClient20bTestClass.oadrRegisteredReport(payload);
	}

	@Test
	public void oadrCancelReportTest() throws Oadr20bException, Oadr20bHttpLayerException, Oadr20bXMLSignatureException,
			Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrCanceledReportType build = Oadr20bEiReportBuilders
				.newOadr20bCanceledReportBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrCanceledReportType, JAXBElement<OadrCanceledReportType>>post(
				ArgumentMatchers.<JAXBElement<OadrCanceledReportType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrCancelReportType payload = new OadrCancelReportType();
		oadrHttpVenClient20bTestClass.oadrCancelReport(payload);
	}

	@Test
	public void oadrCanceledReportTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrResponseType mockOadrResponseType = Oadr20bResponseBuilders
				.newOadr20bResponseBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrResponseType, JAXBElement<OadrResponseType>>post(
				ArgumentMatchers.<JAXBElement<OadrResponseType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockOadrResponseType);

		OadrCanceledReportType payload = new OadrCanceledReportType();
		oadrHttpVenClient20bTestClass.oadrCanceledReport(payload);
	}

	@Test
	public void oadrPollTest() throws Oadr20bException, Oadr20bHttpLayerException, Oadr20bXMLSignatureException,
			Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrHttpVenClient20b adrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<Object, JAXBElement<Object>>post(ArgumentMatchers.<JAXBElement<Object>>any(),
				ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new Object());

		OadrPollType payload = new OadrPollType();
		adrHttpVenClient20bTestClass.oadrPoll(payload);
	}

	@Test
	public void oadrCreatePartyRegistrationTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrCreatedPartyRegistrationType build = Oadr20bEiRegisterPartyBuilders
				.newOadr20bCreatedPartyRegistrationBuilder(
						Oadr20bResponseBuilders.newOadr20bEiResponseBuilder("", 200).build(), "venId", "vtnId")
				.build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrCreatedPartyRegistrationType, JAXBElement<OadrCreatedPartyRegistrationType>>post(
				ArgumentMatchers.<JAXBElement<OadrCreatedPartyRegistrationType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrCreatePartyRegistrationType payload = new OadrCreatePartyRegistrationType();
		oadrHttpVenClient20bTestClass.oadrCreatePartyRegistration(payload);
	}

	@Test
	public void oadrCancelPartyRegistrationTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrCanceledPartyRegistrationType build = Oadr20bEiRegisterPartyBuilders
				.newOadr20bCanceledPartyRegistrationBuilder(Oadr20bResponseBuilders.newOadr20bEiResponseOK(""), "venId",
						"vtnId")
				.build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrCanceledPartyRegistrationType, JAXBElement<OadrCanceledPartyRegistrationType>>post(
				ArgumentMatchers.<JAXBElement<OadrCanceledPartyRegistrationType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrCancelPartyRegistrationType payload = new OadrCancelPartyRegistrationType();
		oadrHttpVenClient20bTestClass.oadrCancelPartyRegistration(payload);
	}

	@Test
	public void oadrResponseReregisterPartyTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrResponseType mockOadrResponseType = Oadr20bResponseBuilders
				.newOadr20bResponseBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrResponseType, JAXBElement<OadrResponseType>>post(
				ArgumentMatchers.<JAXBElement<OadrResponseType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockOadrResponseType);

		OadrResponseType payload = new OadrResponseType();
		oadrHttpVenClient20bTestClass.oadrResponseReregisterParty(payload);
	}

	@Test
	public void oadrCanceledPartyRegistrationTypeTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrResponseType mockOadrResponseType = Oadr20bResponseBuilders
				.newOadr20bResponseBuilder("", HttpURLConnection.HTTP_OK, "venId").build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrResponseType, JAXBElement<OadrResponseType>>post(
				ArgumentMatchers.<JAXBElement<OadrResponseType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(mockOadrResponseType);

		OadrCanceledPartyRegistrationType payload = new OadrCanceledPartyRegistrationType();
		oadrHttpVenClient20bTestClass.oadrCanceledPartyRegistrationType(payload);
	}

	@Test
	public void oadrQueryRegistrationTypeTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrCreatedPartyRegistrationType build = Oadr20bEiRegisterPartyBuilders
				.newOadr20bCreatedPartyRegistrationBuilder(
						Oadr20bResponseBuilders.newOadr20bEiResponseBuilder("", 200).build(), "venId", "vtnId")
				.build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrCreatedPartyRegistrationType, JAXBElement<OadrCreatedPartyRegistrationType>>post(
				ArgumentMatchers.<JAXBElement<OadrCreatedPartyRegistrationType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrQueryRegistrationType payload = new OadrQueryRegistrationType();
		oadrHttpVenClient20bTestClass.oadrQueryRegistrationType(payload);
	}

	@Test
	public void oadrCreateOptTest() throws Oadr20bException, Oadr20bHttpLayerException, Oadr20bXMLSignatureException,
			Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrCreatedOptType build = Oadr20bEiOptBuilders.newOadr20bCreatedOptBuilder("", HttpURLConnection.HTTP_OK, "OPT_IN")
				.build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrCreatedOptType, JAXBElement<OadrCreatedOptType>>post(
				ArgumentMatchers.<JAXBElement<OadrCreatedOptType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrCreateOptType payload = new OadrCreateOptType();
		oadrHttpVenClient20bTestClass.oadrCreateOpt(payload);
	}

	@Test
	public void oadrCancelOptTypeTest() throws Oadr20bException, Oadr20bHttpLayerException,
			Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {

		OadrHttpClient20b oadrHttpClient20b = Mockito.mock(OadrHttpClient20b.class);

		OadrCanceledOptType build = Oadr20bEiOptBuilders.newOadr20bCanceledOptBuilder("", HttpURLConnection.HTTP_OK, "OPT_IN")
				.build();
		OadrHttpVenClient20b oadrHttpVenClient20bTestClass = new OadrHttpVenClient20b(oadrHttpClient20b);

		when(oadrHttpClient20b.<OadrCanceledOptType, JAXBElement<OadrCanceledOptType>>post(
				ArgumentMatchers.<JAXBElement<OadrCanceledOptType>>any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
						.thenReturn(build);

		OadrCancelOptType payload = new OadrCancelOptType();
		oadrHttpVenClient20bTestClass.oadrCancelOptType(payload);
	}

}
