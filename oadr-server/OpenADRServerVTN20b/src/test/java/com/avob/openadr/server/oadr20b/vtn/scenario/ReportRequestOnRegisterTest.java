package com.avob.openadr.server.oadr20b.vtn.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import com.avob.openadr.model.oadr20b.builders.Oadr20bEiBuilders;
import com.avob.openadr.model.oadr20b.builders.Oadr20bEiReportBuilders;
import com.avob.openadr.model.oadr20b.ei.ReadingTypeEnumeratedType;
import com.avob.openadr.model.oadr20b.ei.ReportEnumeratedType;
import com.avob.openadr.model.oadr20b.ei.ReportNameEnumeratedType;
import com.avob.openadr.model.oadr20b.oadr.OadrCreatedReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrRegisterReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrRegisteredReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrReportDescriptionType;
import com.avob.openadr.model.oadr20b.oadr.OadrReportRequestType;
import com.avob.openadr.model.oadr20b.oadr.OadrReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrResponseType;
import com.avob.openadr.model.oadr20b.oadr.OadrUpdateReportType;
import com.avob.openadr.model.oadr20b.oadr.OadrUpdatedReportType;
import com.avob.openadr.model.oadr20b.siscale.SiScaleCodeType;
import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.models.ven.VenDto;
import com.avob.openadr.server.common.vtn.service.VenService;
import com.avob.openadr.server.oadr20b.vtn.AbstractVtn20bTest;
import com.avob.openadr.server.oadr20b.vtn.VTN20bSecurityApplicationTest;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapability;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescription;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloatDto;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequest;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestSpecifier;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestSpecifierDao;
import com.avob.openadr.server.oadr20b.vtn.service.XmlSignatureService;
import com.avob.openadr.server.oadr20b.vtn.service.ei.Oadr20bVTNEiReportService;
import com.avob.openadr.server.oadr20b.vtn.service.report.OtherReportCapabilityDescriptionService;
import com.avob.openadr.server.oadr20b.vtn.service.report.OtherReportCapabilityService;
import com.avob.openadr.server.oadr20b.vtn.service.report.OtherReportDataFloatService;
import com.avob.openadr.server.oadr20b.vtn.service.report.OtherReportRequestService;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrDataBaseSetup;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrMockEiHttpMvc;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrMockEiXmpp;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrMockHttpMvc;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrMockHttpVenMvc;
import com.avob.openadr.server.oadr20b.vtn.utils.OadrMockVen;

/**
 * oadr.report.requestOnRegister 를 켰을 때 리포트 등록 응답(oadrRegisteredReport)에 리포트 요청이 실리고,
 * 그 요청으로 올린 값이 저장되어 요청별 받은 값 목록(/Ven/{venID}/report/requested/{reportRequestId}/data/float)에 나오는지 본다.
 *
 * 등록 응답의 요청을 받아 쓰는 VEN 이 보내는 모양으로 등록한다
 * (METADATA_TELEMETRY_USAGE, rID 마다 oadrSamplingRate 의 oadrMaxPeriod 에 주기).
 * 설정은 컨텍스트를 새로 띄우지 않고 서비스 필드를 바꿔 켠다. 테스트 DB 는 컨텍스트가 뜰 때마다 create-drop 이라
 * 설정이 다른 컨텍스트가 하나 더 뜨면 다른 테스트가 쓰는 데이터가 지워진다.
 * 서비스 빈은 트랜잭션 프록시라 필드는 프록시가 아니라 원래 객체에 넣는다(AopTestUtils)
 */
@ContextConfiguration(classes = { VTN20bSecurityApplicationTest.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ReportRequestOnRegisterTest extends AbstractVtn20bTest {

	private static final String REPORT_SPECIFIER_ID = "TELEMETRY_TU";
	private static final String GRANULARITY = "PT15M";
	private static final String REGISTER_REQUEST_ID_PREFIX = "register-";

	@Resource
	private Oadr20bVTNEiReportService reportService;

	@Resource
	private VenService venService;

	@Resource
	private OtherReportCapabilityService otherReportCapabilityService;

	@Resource
	private OtherReportCapabilityDescriptionService otherReportCapabilityDescriptionService;

	@Resource
	private OtherReportRequestService otherReportRequestService;

	@Resource
	private OtherReportRequestSpecifierDao otherReportRequestSpecifierDao;

	@Resource
	private OtherReportDataFloatService otherReportDataService;

	@Resource
	private XmlSignatureService xmlSignatureService;

	@Resource
	private OadrMockEiHttpMvc oadrMockEiHttpMvc;

	@Resource
	private OadrMockEiXmpp oadrMockEiXmpp;

	@Resource
	private OadrMockHttpMvc oadrMockHttpMvc;

	@Resource
	private OadrMockHttpVenMvc oadrMockHttpVenMvc;

	@Test
	public void requestOnRegister() throws Exception {
		VenDto venDto = oadrMockHttpVenMvc.getVen(OadrDataBaseSetup.ADMIN_SECURITY_SESSION,
				OadrDataBaseSetup.VEN_HTTP_PULL, HttpServletResponse.SC_OK);
		OadrMockVen mockVen = new OadrMockVen(venDto, OadrDataBaseSetup.ANOTHER_VEN_SECURITY_SESSION, oadrMockEiHttpMvc,
				oadrMockEiXmpp, xmlSignatureService);
		Ven ven = venService.findOneByUsername(OadrDataBaseSetup.VEN_HTTP_PULL);

		Oadr20bVTNEiReportService target = AopTestUtils.getUltimateTargetObject(reportService);
		ReflectionTestUtils.setField(target, "requestOnRegister", true);
		try {
			OadrRegisterReportType register = Oadr20bEiReportBuilders
					.newOadr20bRegisterReportBuilder("requestId", mockVen.getVenId()).addOadrReport(metadataReport())
					.build();

			// 등록 응답에 명세 하나에 대한 요청 하나. 주기와 reportBackDuration 은 oadrMaxPeriod, rID 두 개
			OadrRegisteredReportType registered = mockVen.report(register, HttpServletResponse.SC_OK,
					OadrRegisteredReportType.class);
			assertEquals(String.valueOf(HttpServletResponse.SC_OK), registered.getEiResponse().getResponseCode());
			assertEquals(1, registered.getOadrReportRequest().size());
			OadrReportRequestType request = registered.getOadrReportRequest().get(0);
			String reportRequestId = request.getReportRequestID();
			assertTrue(reportRequestId.startsWith(REGISTER_REQUEST_ID_PREFIX));
			assertEquals(REPORT_SPECIFIER_ID, request.getReportSpecifier().getReportSpecifierID());
			assertEquals(GRANULARITY, request.getReportSpecifier().getGranularity().getDuration());
			assertEquals(GRANULARITY, request.getReportSpecifier().getReportBackDuration().getDuration());
			assertEquals(2, request.getReportSpecifier().getSpecifierPayload().size());

			// VTN 에는 구독 하나, rID 마다 이력 저장
			List<OtherReportRequest> requests = registerRequests(ven);
			assertEquals(1, requests.size());
			assertEquals(reportRequestId, requests.get(0).getReportRequestId());
			List<OtherReportRequestSpecifier> specifiers = otherReportRequestSpecifierDao.findByRequest(requests.get(0));
			assertEquals(2, specifiers.size());
			specifiers.forEach(specifier -> assertTrue(specifier.getArchived()));

			// 다시 등록해도 같은 요청을 다시 쓴다
			registered = mockVen.report(register, HttpServletResponse.SC_OK, OadrRegisteredReportType.class);
			assertEquals(1, registered.getOadrReportRequest().size());
			assertEquals(reportRequestId, registered.getOadrReportRequest().get(0).getReportRequestID());
			assertEquals(1, registerRequests(ven).size());

			// VEN 이 요청을 받았다고 알린다
			OadrCreatedReportType created = Oadr20bEiReportBuilders
					.newOadr20bCreatedReportBuilder("requestId", HttpServletResponse.SC_OK, mockVen.getVenId())
					.addPendingReportRequestId(reportRequestId).build();
			OadrResponseType response = mockVen.report(created, HttpServletResponse.SC_OK, OadrResponseType.class);
			assertEquals(String.valueOf(HttpServletResponse.SC_OK), response.getEiResponse().getResponseCode());

			// 그 요청으로 값을 올리면 저장되고 요청별 받은 값 목록에 나온다
			long start = System.currentTimeMillis();
			OadrReportType report = Oadr20bEiReportBuilders
					.newOadr20bUpdateReportOadrReportBuilder("reportId", REPORT_SPECIFIER_ID, reportRequestId,
							ReportNameEnumeratedType.TELEMETRY_USAGE, start, start, GRANULARITY)
					.addInterval(Oadr20bEiBuilders
							.newOadr20bReportIntervalTypeBuilder("0", start, GRANULARITY, "rid1", 100L, 0F, 3F).build())
					.build();
			OadrUpdateReportType update = Oadr20bEiReportBuilders
					.newOadr20bUpdateReportBuilder(reportRequestId, mockVen.getVenId()).addReport(report).build();
			OadrUpdatedReportType updated = mockVen.report(update, HttpServletResponse.SC_OK,
					OadrUpdatedReportType.class);
			assertEquals(String.valueOf(HttpServletResponse.SC_OK), updated.getEiResponse().getResponseCode());

			List<OtherReportDataFloatDto> data = oadrMockHttpMvc.getRestJsonControllerAndExpectList(
					OadrDataBaseSetup.ADMIN_SECURITY_SESSION,
					"/Ven/" + mockVen.getVenId() + "/report/requested/" + reportRequestId + "/data/float",
					HttpServletResponse.SC_OK, OtherReportDataFloatDto.class);
			assertEquals(1, data.size());
			assertEquals("rid1", data.get(0).getRid());
			assertEquals(3F, data.get(0).getValue());

		} finally {
			ReflectionTestUtils.setField(target, "requestOnRegister", false);
			cleanup(ven);
		}
	}

	private List<OtherReportRequest> registerRequests(Ven ven) {
		return otherReportRequestService.findBySource(ven).stream()
				.filter(request -> request.getReportRequestId() != null
						&& request.getReportRequestId().startsWith(REGISTER_REQUEST_ID_PREFIX))
				.collect(Collectors.toList());
	}

	// 다른 테스트가 같은 VEN 의 리포트 명세, 요청 개수를 세므로 여기서 만든 것만 지운다
	private void cleanup(Ven ven) {
		for (OtherReportRequest request : registerRequests(ven)) {
			otherReportDataService.findRecentByReportRequestId(ven.getUsername(), request.getReportRequestId(), 1000)
					.forEach(data -> otherReportDataService.delete(data.getId()));
			otherReportRequestSpecifierDao.deleteByRequest(request);
			otherReportRequestService.delete(request.getId());
		}
		OtherReportCapability capability = otherReportCapabilityService
				.findOneBySourceUsernameAndReportSpecifierId(ven.getUsername(), REPORT_SPECIFIER_ID);
		if (capability != null) {
			for (OtherReportCapabilityDescription description : otherReportCapabilityDescriptionService
					.findByOtherReportCapability(capability)) {
				otherReportCapabilityDescriptionService.delete(description.getId());
			}
			otherReportCapabilityService.delete(capability.getId());
		}
	}

	// 등록 응답의 요청을 받아 쓰는 VEN 의 METADATA 리포트 모양(rID 마다 자원 이름, 주기)
	private static OadrReportType metadataReport() {
		return Oadr20bEiReportBuilders
				.newOadr20bRegisterReportOadrReportBuilder(REPORT_SPECIFIER_ID,
						ReportNameEnumeratedType.METADATA_TELEMETRY_USAGE, System.currentTimeMillis())
				.addReportDescription(description("rid1")).addReportDescription(description("rid2")).build();
	}

	private static OadrReportDescriptionType description(String rid) {
		return Oadr20bEiReportBuilders
				.newOadr20bOadrReportDescriptionBuilder(rid, ReportEnumeratedType.USAGE,
						ReadingTypeEnumeratedType.DIRECT_READ)
				.withEnergyRealBase(SiScaleCodeType.KILO)
				.withDataSource(Oadr20bEiBuilders.newOadr20bEiTargetTypeBuilder().addResourceId("Resource1").build())
				.withOadrSamplingRate("PT1M", GRANULARITY, false).build();
	}
}
