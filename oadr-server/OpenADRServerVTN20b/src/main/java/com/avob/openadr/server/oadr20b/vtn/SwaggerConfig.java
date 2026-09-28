package com.avob.openadr.server.oadr20b.vtn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * REST API 문서.
 *
 * springfox 는 Spring Boot 3 에서 동작하지 않아 springdoc 으로 교체했다.
 * springfox 는 스프링 내부 구조를 직접 건드리는 방식이라 Boot 3 에서 기동조차 못 한다.
 * springdoc 은 OpenAPI 3 를 쓰고 표준 애노테이션만 본다.
 *
 * 예전 Docket 설정이 하던 일은 두 가지였다.
 * 하나는 OpenADR 프로토콜 엔드포인트와 error, manage 경로를 문서에서 빼는 것,
 * 다른 하나는 API 정보를 채우는 것이다. 둘 다 아래에 그대로 옮겼다.
 *
 * vtn.swagger 프로퍼티가 있을 때만 켜지는 것도 예전과 같다.
 * 화면은 /swagger-ui/index.html 에서 열린다.
 */
@ConditionalOnProperty(name = "vtn.swagger")
@Configuration
public class SwaggerConfig {

	/**
	 * 이름이 겹치는 오퍼레이션 중에서 꼬리표 없는 이름을 가져갈 쪽을 직접 지정한다.
	 *
	 * 자바 메서드 이름이 컨트롤러를 넘나들며 겹치는 경우가 있다. 예를 들어
	 * viewReportRequest 는 Oadr20bVtnController 와 Oadr20bVenController 양쪽에 있다.
	 * 어느 쪽이 대표 이름을 갖느냐는 경로만 보고는 정할 수 없다. 화면이 실제로 무엇을
	 * 부르는지가 기준이고, 그건 코드에 적혀 있으니 여기에 적어 둔다.
	 *
	 * 키는 "HTTP메서드 경로" 형식이다.
	 */
	private static final Map<String, String> PINNED_OPERATION_IDS = Map.of(
			// 이벤트 상세의 VEN 응답 탭. /venResponse/{username} 과 메서드 이름이 같다
			"GET /DemandResponseEvent/{id}/venResponse", "readVenDemandResponseEventUsingGET",
			// VEN 상세의 Requests 탭. /Vtn/report/requested 와 메서드 이름이 같다
			"GET /Ven/{venID}/report/requested", "viewReportRequestUsingGET",
			// VEN 상세의 Reports 탭. /Report/available/search 와 메서드 이름이 같다
			"GET /Ven/{venID}/report/available", "viewOtherReportCapabilityUsingGET",
			// 같은 탭의 description 목록. /Vtn/report/available/{id} 와 메서드 이름이 같다
			"GET /Ven/{venID}/report/available/description", "viewOtherReportCapabilityDescriptionUsingGET");

	@Bean
	public OpenAPI vtnOpenApi() {
		Contact contact = new Contact().name("Bertrand Zanni").url("http://avob.com").email("bzanni@avob.com");

		Info info = new Info().title("Avob VTN Rest API").description("Description").termsOfService("API TOS")
				.contact(contact).license(new License().name("License of API").url("API license URL"));

		return new OpenAPI().info(info);
	}

	/**
	 * operationId 를 springfox 형식으로 맞추고, 문서 전체에서 유일하게 만든다.
	 *
	 * 프론트엔드가 swagger-client 로 API 를 부르는데, 호출하는 이름이
	 * registeredUserUsingGET 처럼 springfox 가 만들던 형식이다.
	 * springdoc 은 메서드 이름 그대로(registeredUser) 내보내기 때문에
	 * 그냥 바꾸면 화면의 API 호출이 전부 깨진다.
	 * 프론트엔드 100여 군데를 고치는 대신 여기서 뒤에 UsingGET 같은 꼬리를 붙인다.
	 *
	 * 유일성이 특히 중요하다. swagger-client 는 스펙을 읽을 때 operationId 가
	 * 문서 안에서 두 번 이상 나오면 겹친 것들 전부에 1, 2 를 붙여 이름을 바꿔 버린다.
	 * 꼬리표 없는 원래 이름은 아예 사라진다. 그래서 화면에서는
	 * "xxxUsingGET is not a function" 이 나고, 태그가 달라도 소용이 없다.
	 * (태그는 client.apis 를 나누기만 하고, 이름 중복 판정은 문서 전체로 한다.)
	 *
	 * 오퍼레이션 하나씩 보는 OperationCustomizer 대신 문서 전체를 보는
	 * OpenApiCustomizer 를 쓰는 이유가 여기에 있다. 더불어 HTTP 메서드도
	 * 정확해진다. 예전에는 핸들러의 @RequestMapping 에서 method()[0] 을 읽었는데,
	 * method 를 안 적은 매핑은 모든 verb 를 받으므로 PUT 도 DELETE 도 UsingGET 이
	 * 되어 버렸다. 여기서는 문서에서 그 오퍼레이션이 놓인 칸으로 verb 를 정한다.
	 *
	 * 겹칠 때 누가 대표 이름을 갖는지는 PINNED_OPERATION_IDS 가 먼저 정하고,
	 * 지정이 없으면 경로 변수 개수, 경로 길이, 사전순으로 제일 단순한 쪽이 갖는다.
	 * 나머지는 springfox 가 하던 대로 _1 부터 붙는다. 문서를 다시 만들어도 결과가 같다.
	 */
	@Bean
	public OpenApiCustomizer springfoxStyleOperationId() {
		return openApi -> {
			if (openApi.getPaths() == null) {
				return;
			}

			Map<String, List<Candidate>> byName = new LinkedHashMap<>();

			openApi.getPaths()
					.forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
						String base = operation.getOperationId();
						if (base == null) {
							return;
						}
						String pinned = PINNED_OPERATION_IDS.get(httpMethod.name() + " " + path);
						String name;
						boolean isPinned;
						if (pinned != null) {
							name = pinned;
							isPinned = true;
						} else if (base.contains("Using")) {
							// @Operation(operationId = ...) 으로 직접 지정한 이름은 그대로 둔다
							name = base;
							isPinned = true;
						} else {
							// springdoc 은 자바 메서드 이름이 겹치면 자기가 먼저 _1 을 붙인다.
							// 어느 쪽에 붙을지가 핸들러 열거 순서에 달려 있어 재기동마다 바뀔 수
							// 있으므로, 그 꼬리표는 떼고 아래에서 우리 규칙으로 다시 붙인다.
							name = base.replaceAll("_\\d+$", "") + "Using" + httpMethod.name();
							isPinned = false;
						}
						byName.computeIfAbsent(name, k -> new ArrayList<>())
								.add(new Candidate(path, operation, isPinned));
					}));

			byName.forEach((name, candidates) -> {
				// 지정된 것이 먼저, 그다음은 제일 단순한 경로 순
				candidates.sort(Comparator.comparing((Candidate c) -> c.pinned ? 0 : 1)
						.thenComparingInt(c -> countPathVariables(c.path)).thenComparingInt(c -> c.path.length())
						.thenComparing(c -> c.path));
				for (int i = 0; i < candidates.size(); i++) {
					candidates.get(i).operation.setOperationId(i == 0 ? name : name + "_" + i);
				}
			});
		};
	}

	private static int countPathVariables(String path) {
		int count = 0;
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '{') {
				count++;
			}
		}
		return count;
	}

	/**
	 * 정렬해서 대표 이름을 정하기 위해 경로와 오퍼레이션을 같이 들고 다닌다.
	 */
	private static final class Candidate {
		private final String path;
		private final Operation operation;
		private final boolean pinned;

		private Candidate(String path, Operation operation, boolean pinned) {
			this.path = path;
			this.operation = operation;
			this.pinned = pinned;
		}
	}

	/**
	 * 내부용 경로는 문서에서 뺀다.
	 * 예전 springfox 의 PathSelectors.regex 를 뒤집어 쓰던 것과 같은 규칙이다.
	 */
	@Bean
	public GroupedOpenApi vtnApi() {
		return GroupedOpenApi.builder().group("vtn").pathsToMatch("/**")
				.pathsToExclude("/**/OpenADR2/**", "/**/error/**", "/**/manage/**").build();
	}

}
