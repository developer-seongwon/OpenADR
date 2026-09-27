package com.avob.openadr.server.oadr20b.vtn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

/**
 * operationId 를 만드는 규칙을 고정한다.
 *
 * 프론트엔드가 swagger-client 로 API 를 부를 때 쓰는 이름이 여기서 나온다.
 * 이름이 어긋나면 화면에서 그 기능만 조용히 죽는데, 서버 로그에는 아무것도 안 남고
 * 네트워크 요청조차 나가지 않아서 원인을 찾기가 아주 고약하다.
 * 그래서 규칙 자체를 테스트로 묶어 둔다.
 */
public class SwaggerConfigTest {

	private OpenAPI customize(OpenAPI openApi) {
		OpenApiCustomizer customizer = new SwaggerConfig().springfoxStyleOperationId();
		customizer.customise(openApi);
		return openApi;
	}

	private Operation operation(String operationId, String tag) {
		Operation operation = new Operation();
		operation.setOperationId(operationId);
		operation.setTags(List.of(tag));
		return operation;
	}

	/**
	 * springdoc 은 자바 메서드 이름을 그대로 쓴다. 프론트엔드는 springfox 형식을 기대한다.
	 */
	@Test
	public void appendsHttpMethodSuffix() {
		Paths paths = new Paths();
		paths.addPathItem("/Ven/", new PathItem().get(operation("listVen", "ven-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		assertEquals("listVenUsingGET", openApi.getPaths().get("/Ven/").getGet().getOperationId());
	}

	/**
	 * verb 는 핸들러 애노테이션이 아니라 문서에서 그 오퍼레이션이 놓인 자리에서 가져온다.
	 *
	 * method 를 안 적은 @RequestMapping 은 모든 verb 를 받는다. 예전 구현은 애노테이션의
	 * method()[0] 을 읽어서 그런 매핑이 전부 UsingGET 이 됐고, 같은 경로의 PUT 과 DELETE 가
	 * 같은 이름을 갖게 됐다.
	 */
	@Test
	public void derivesHttpMethodFromDocumentNotAnnotation() {
		Paths paths = new Paths();
		paths.addPathItem("/auth/vhost", new PathItem().get(operation("vhost", "auth-controller"))
				.post(operation("vhost", "auth-controller")).delete(operation("vhost", "auth-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		PathItem item = openApi.getPaths().get("/auth/vhost");
		assertEquals("vhostUsingGET", item.getGet().getOperationId());
		assertEquals("vhostUsingPOST", item.getPost().getOperationId());
		assertEquals("vhostUsingDELETE", item.getDelete().getOperationId());
	}

	/**
	 * 오버로드된 컨트롤러 메서드는 같은 이름으로 두 번 나온다.
	 *
	 * swagger-client 는 operationId 를 키로 쓰는 맵을 만들기 때문에 하나가 다른 하나를
	 * 덮어 버린다. 실제로 이것 때문에 이벤트 상세의 VEN 응답 탭이 죽었다.
	 * 경로가 단순한 쪽이 꼬리표 없는 이름을 갖고, 나머지는 _1 부터 붙는다.
	 */
	@Test
	public void disambiguatesOverloadedOperationsWithinSameTag() {
		Paths paths = new Paths();
		paths.addPathItem("/DemandResponseEvent/{id}/venResponse/{username}",
				new PathItem().get(operation("readVenDemandResponseEvent", "demand-response-controller")));
		paths.addPathItem("/DemandResponseEvent/{id}/venResponse",
				new PathItem().get(operation("readVenDemandResponseEvent", "demand-response-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		assertEquals("readVenDemandResponseEventUsingGET",
				openApi.getPaths().get("/DemandResponseEvent/{id}/venResponse").getGet().getOperationId());
		assertEquals("readVenDemandResponseEventUsingGET_1", openApi.getPaths()
				.get("/DemandResponseEvent/{id}/venResponse/{username}").getGet().getOperationId());
	}

	/**
	 * 태그가 달라도 이름이 겹치면 충돌이다.
	 *
	 * swagger-client 는 client.apis 를 태그로 나누지만, operationId 중복 판정은
	 * 문서 전체로 한다. 두 번 이상 나오는 이름은 겹친 것들 전부에 1, 2 를 붙여
	 * 이름을 바꿔 버리고 원래 이름은 사라진다. 그래서 태그가 다르다고 안심할 수 없다.
	 *
	 * 어느 쪽이 대표 이름을 갖는지는 PINNED_OPERATION_IDS 가 정한다.
	 * 여기서는 화면이 실제로 부르는 /Ven 쪽이 대표 이름을 갖는지 본다.
	 */
	@Test
	public void sameNameInDifferentTagsIsStillAConflict() {
		Paths paths = new Paths();
		paths.addPathItem("/Vtn/report/requested",
				new PathItem().get(operation("viewReportRequest", "oadr-20b-vtn-controller")));
		paths.addPathItem("/Ven/{venID}/report/requested",
				new PathItem().get(operation("viewReportRequest", "oadr-20b-ven-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		assertEquals("viewReportRequestUsingGET",
				openApi.getPaths().get("/Ven/{venID}/report/requested").getGet().getOperationId());
		assertEquals("viewReportRequestUsingGET_1",
				openApi.getPaths().get("/Vtn/report/requested").getGet().getOperationId());
	}

	/**
	 * 경로 변수가 없는 쪽이 더 단순해 보여도, 화면이 부르는 쪽이 대표 이름을 갖는다.
	 *
	 * viewOtherReportCapability 는 /Report/available/search 에도 있다. 경로 변수가 없어서
	 * 단순함 기준으로는 그쪽이 이기는데, 화면은 /Ven/{venID}/report/available 를 부른다.
	 * 그래서 지정이 필요하다.
	 */
	@Test
	public void pinnedNameWinsOverTheSimplerPath() {
		Paths paths = new Paths();
		paths.addPathItem("/Report/available/search",
				new PathItem().get(operation("viewOtherReportCapability", "report-controller")));
		paths.addPathItem("/Ven/{venID}/report/available",
				new PathItem().get(operation("viewOtherReportCapability", "oadr-20b-ven-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		assertEquals("viewOtherReportCapabilityUsingGET",
				openApi.getPaths().get("/Ven/{venID}/report/available").getGet().getOperationId());
		assertEquals("viewOtherReportCapabilityUsingGET_1",
				openApi.getPaths().get("/Report/available/search").getGet().getOperationId());
	}

	/**
	 * 문서 전체에서 같은 operationId 가 두 번 나오는 일이 없어야 한다.
	 */
	@Test
	public void everyOperationIdIsUniqueAcrossTheDocument() {
		Paths paths = new Paths();
		paths.addPathItem("/OpenADR2/Simple/2.0b/EiEvent",
				new PathItem().post(operation("request", "ei-event-controller")));
		paths.addPathItem("/OpenADR2/Simple/2.0b/EiReport",
				new PathItem().post(operation("request", "ei-report-controller")));
		paths.addPathItem("/OpenADR2/Simple/2.0b/OadrPoll",
				new PathItem().post(operation("request", "oadr-poll-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		Set<String> ids = new HashSet<>();
		openApi.getPaths().values()
				.forEach(item -> item.readOperations().forEach(op -> assertTrue(
						ids.add(op.getOperationId()), "operationId 가 중복됐다: " + op.getOperationId())));
		assertEquals(3, ids.size());
	}

	/**
	 * springdoc 이 먼저 붙인 _1 은 떼고 다시 판단한다.
	 *
	 * springdoc 은 자바 메서드 이름이 겹치면 자기가 먼저 _1 을 붙인다.
	 * 문제는 둘 중 어디에 붙을지가 핸들러 열거 순서에 달려 있어서 재기동마다
	 * 뒤집힐 수 있다는 것이다. 그러면 화면이 부르는 이름도 같이 뒤집힌다.
	 * 그래서 그 꼬리표는 떼고 우리 규칙으로 다시 붙인다.
	 * 여기서는 springdoc 이 /Ven 쪽에 _1 을 붙여 놨어도 결과가 같아야 한다.
	 */
	@Test
	public void stripsSpringdocOwnDisambiguationSuffix() {
		Paths paths = new Paths();
		paths.addPathItem("/Vtn/report/requested",
				new PathItem().get(operation("viewReportRequest", "oadr-20b-vtn-controller")));
		paths.addPathItem("/Ven/{venID}/report/requested",
				new PathItem().get(operation("viewReportRequest_1", "oadr-20b-ven-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		assertEquals("viewReportRequestUsingGET",
				openApi.getPaths().get("/Ven/{venID}/report/requested").getGet().getOperationId());
		assertEquals("viewReportRequestUsingGET_1",
				openApi.getPaths().get("/Vtn/report/requested").getGet().getOperationId());
	}

	/**
	 * 이미 Using 이 들어간 이름은 손대지 않는다.
	 * @Operation(operationId = ...) 으로 직접 지정한 경우를 존중한다.
	 */
	@Test
	public void leavesExplicitOperationIdAlone() {
		Paths paths = new Paths();
		paths.addPathItem("/Ven/", new PathItem().get(operation("listVenUsingGET", "ven-controller")));

		OpenAPI openApi = customize(new OpenAPI().paths(paths));

		assertEquals("listVenUsingGET", openApi.getPaths().get("/Ven/").getGet().getOperationId());
	}

}
