package com.avob.openadr.server.oadr20b.vtn.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import jakarta.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.service.VenService;
import com.avob.openadr.server.oadr20b.vtn.VTN20bSecurityApplicationTest;
import com.avob.openadr.server.oadr20b.vtn.models.venopt.VenOpt;
import com.avob.openadr.server.oadr20b.vtn.models.venopt.VenOptDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapability;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescription;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.OtherReportCapabilityDescriptionDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloat;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.data.OtherReportDataFloatDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequest;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestDao;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestSpecifier;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.request.OtherReportRequestSpecifierDao;

/**
 * VEN 삭제가 VTN20b 쪽 테이블에 걸려 터지지 않는지 본다.
 *
 * VTNCommon 의 VenService 는 venresource 와 vendemandresponseevent 만 비운다.
 * VTN20b 의 otherreportcapability, otherreportrequest, venopt 는 전부
 * ON DELETE NO ACTION 이라, 행이 남으면 삭제가 외래키 위반으로 500 이 났다.
 * 리포트 명세를 한 번이라도 올린 VEN 은 화면에서도 API 로도 못 지웠다.
 *
 * Oadr20bVenDeleteHandler 가 VenDeleteHandler 로 등록돼서 그 앞에 치운다.
 * 그 연결이 끊어지면 이 테스트가 먼저 깨진다.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { VTN20bSecurityApplicationTest.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class VenDeleteTest {

	@Resource
	private VenService venService;

	@Resource
	private OtherReportCapabilityDao otherReportCapabilityDao;

	@Resource
	private OtherReportCapabilityDescriptionDao otherReportCapabilityDescriptionDao;

	@Resource
	private OtherReportRequestDao otherReportRequestDao;

	@Resource
	private OtherReportRequestSpecifierDao otherReportRequestSpecifierDao;

	@Resource
	private OtherReportDataFloatDao otherReportDataFloatDao;

	@Resource
	private VenOptDao venOptDao;

	private Ven createVen(String username) {
		return venService.save(venService.prepare(username, username));
	}

	@Test
	public void deleteVenWithReportCapability() {
		String username = "deleteTestVen-" + UUID.randomUUID();
		Ven ven = createVen(username);

		OtherReportCapability capability = new OtherReportCapability(ven);
		capability.setReportSpecifierId("spec");
		capability = otherReportCapabilityDao.save(capability);

		OtherReportCapabilityDescription description = new OtherReportCapabilityDescription(capability);
		description.setRid("rid");
		description = otherReportCapabilityDescriptionDao.save(description);

		OtherReportRequest request = new OtherReportRequest();
		request.setSource(ven);
		request.setOtherReportCapability(capability);
		request.setReportRequestId("reportRequestId");
		request = otherReportRequestDao.save(request);

		OtherReportRequestSpecifier specifier = new OtherReportRequestSpecifier();
		specifier.setRequest(request);
		specifier.setOtherReportCapabilityDescription(description);
		otherReportRequestSpecifierDao.save(specifier);

		OtherReportDataFloat data = new OtherReportDataFloat();
		data.setVenId(username);
		data.setReportSpecifierId("spec");
		data.setReportRequestId("reportRequestId");
		data.setRid("rid");
		data.setValue(1F);
		otherReportDataFloatDao.save(data);

		venService.delete(ven);

		assertNull(venService.findOneByUsername(username));
		assertTrue(otherReportCapabilityDao.findBySource(ven).isEmpty());
		assertTrue(otherReportRequestDao.findBySource(ven).isEmpty());
		assertTrue(otherReportRequestSpecifierDao.findByRequestSource(ven).isEmpty());
		assertEquals(0, otherReportDataFloatDao.findByVenIdAndReportSpecifierId(username, "spec").size());
	}

	/**
	 * capability 를 못 찾은 채 저장된 요청이 남아 있어도 지워져야 한다.
	 *
	 * 예전에 subscribe 가 그런 행을 만들었다. capability 를 거쳐 찾는 삭제만으로는
	 * 걸러지지 않아서, VEN 을 영영 못 지우는 상태가 됐다.
	 */
	@Test
	public void deleteVenWithOrphanReportRequest() {
		String username = "deleteTestVen-" + UUID.randomUUID();
		Ven ven = createVen(username);

		OtherReportRequest request = new OtherReportRequest();
		request.setSource(ven);
		request.setReportRequestId("orphan");
		otherReportRequestDao.save(request);

		venService.delete(ven);

		assertNull(venService.findOneByUsername(username));
		assertTrue(otherReportRequestDao.findBySource(ven).isEmpty());
	}

	@Test
	public void deleteVenWithOptSchedule() {
		String username = "deleteTestVen-" + UUID.randomUUID();
		Ven ven = createVen(username);

		VenOpt opt = new VenOpt();
		opt.setVen(ven);
		opt.setOptId("optId");
		opt.setStart(System.currentTimeMillis());
		venOptDao.save(opt);

		venService.delete(ven);

		assertNull(venService.findOneByUsername(username));
		assertTrue(venOptDao.findScheduledOpt(username).isEmpty());
	}

}
