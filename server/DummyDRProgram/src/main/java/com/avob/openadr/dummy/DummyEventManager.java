package com.avob.openadr.dummy;

import java.io.File;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.avob.server.oadrvtn20b.api.DemandResponseControllerApi;
import com.avob.server.oadrvtn20b.api.MarketContextControllerApi;
import com.avob.server.oadrvtn20b.handler.ApiClient;
import com.avob.server.oadrvtn20b.handler.ApiException;
import com.avob.server.oadrvtn20b.handler.ApiResponse;
import com.avob.server.oadrvtn20b.model.DemandResponseEventCreateDto;
import com.avob.server.oadrvtn20b.model.DemandResponseEventFilter;
import com.avob.server.oadrvtn20b.model.DemandResponseEventFilter.TypeEnum;
import com.avob.server.oadrvtn20b.model.DemandResponseEventReadDto;
import com.avob.server.oadrvtn20b.model.VenMarketContextDto;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class DummyEventManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DummyEventManager.class);

	private static final String X_TOTAL_COUNT = "X-total-count";

	/** 기동할 때 이벤트를 채워 두는 범위(시간). 이미 있는 이벤트를 찾는 조회 범위도 같아야 한다 */
	private static final int NEXT_HOURS = 24;

	@Resource
	private MarketContextControllerApi marketContextControllerApi;

	@Resource
	private DemandResponseControllerApi demandResponseControllerApi;

	@Resource
	private DummyVTN20bControllerConfig dummyVTN20bControllerConfig;

	private List<DemandResponseEventCreateDto> eventTemplate = new ArrayList<>();;

	@PostConstruct
	public void init() {
		Long marketcontextId;
		try {
			VenMarketContextDto findMarketContextByNameUsingGET = marketContextControllerApi
					.findMarketContextByNameUsingGET(DummyVTN20bControllerConfig.MARKET_CONTEXT);
			marketcontextId = findMarketContextByNameUsingGET.getId();
			LOGGER.warn(
					"Ven market context: " + DummyVTN20bControllerConfig.MARKET_CONTEXT + " is already provisioned");
		} catch (ApiException e) {
			if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
				LOGGER.error(
						"Ven market context: " + DummyVTN20bControllerConfig.MARKET_CONTEXT + " can't be provisioned",
						e);
				return;
			} else {
				VenMarketContextDto dto = new VenMarketContextDto();
				dto.setName(DummyVTN20bControllerConfig.MARKET_CONTEXT);
				dto.setDescription(DummyVTN20bControllerConfig.MARKET_CONTEXT_DESCRIPTION);
				try {
					VenMarketContextDto createMarketContextUsingPOST = marketContextControllerApi
							.createMarketContextUsingPOST(dto);
					marketcontextId = createMarketContextUsingPOST.getId();
				} catch (ApiException e1) {
					LOGGER.error("Ven market context: " + DummyVTN20bControllerConfig.MARKET_CONTEXT
							+ " can't be provisioned", e1);
					return;
				}
			}
		}

		if (marketcontextId == null) {
			LOGGER.error("Ven market context: " + DummyVTN20bControllerConfig.MARKET_CONTEXT + " can't be provisioned");
			return;
		}

		// 생성 모델이 Jackson 모델이라 Gson 대신 생성 클라이언트와 같은 설정의 ObjectMapper 로 읽는다
		ObjectMapper objectMapper = ApiClient.createDefaultObjectMapper();

		for (String filePath : dummyVTN20bControllerConfig.getEventTemplate()) {
			try {
				DemandResponseEventCreateDto fromJson = objectMapper.readValue(new File(filePath),
						DemandResponseEventCreateDto.class);
				eventTemplate.add(fromJson);
			} catch (JacksonException e) {
				LOGGER.error("Event template: " + filePath + " cannot be parsed", e);
				return;
			}

		}

		List<DemandResponseEventReadDto> events = new ArrayList<>();
		DemandResponseEventFilter filter = new DemandResponseEventFilter();
		filter.setType(TypeEnum.MARKET_CONTEXT);
		filter.setValue(DummyVTN20bControllerConfig.MARKET_CONTEXT);

		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime truncatedTo = now.truncatedTo(ChronoUnit.HOURS);
		Long start = truncatedTo.toEpochSecond() * 1000;
		// 원래 6시간만 조회하고 24시간을 만들었다. 6시간 뒤의 이벤트는 못 찾아서 재기동마다 다시 만들었으니
		// 만드는 범위와 같게 조회한다
		Long end = truncatedTo.plusHours(NEXT_HOURS).toEpochSecond() * 1000;
		Integer totalCount;
		int page = 0;
		try {

			do {

				ApiResponse<List<DemandResponseEventReadDto>> response = demandResponseControllerApi
						.searchUsingPOSTWithHttpInfo(Arrays.asList(filter), end, page, null, start);
				totalCount = Integer.valueOf(response.getHeaders().get(X_TOTAL_COUNT).get(0));
				page++;
				// 원래 받은 페이지를 events 에 안 담아서, 이벤트가 하나라도 있으면
				// events.size() 가 0 에 머물러 페이지를 끝없이 요청했다. 빈 페이지가 오면 멈춘다
				List<DemandResponseEventReadDto> data = response.getData();
				if (data == null || data.isEmpty()) {
					break;
				}
				events.addAll(data);
			} while (events.size() < totalCount);

			events.forEach(event -> {
				LOGGER.info(String.format("%s", String.valueOf(event.getId())));
			});

		} catch (ApiException e) {
			LOGGER.error("Market context: " + DummyVTN20bControllerConfig.MARKET_CONTEXT + " events can't be retrieved",
					e);
			return;
		}

		this.ensureEventAreCreatedForNextHour(truncatedTo, events, NEXT_HOURS);

	}

	private void ensureEventAreCreatedForNextHour(OffsetDateTime start, List<DemandResponseEventReadDto> existingEvents,
			int nextXHours) {

		// 원래 existingStart 를 모아 놓고 쓰지 않아서 DummyDRProgram 을 띄울 때마다 같은 이벤트가 또 생겼다.
		// 템플릿마다 시작 시각이 겹칠 수 있으므로(thermostat PT4H, fastDR PT30M 이 둘 다 정시에 시작)
		// 시작 시각과 길이를 함께 본다
		Set<String> existing = new HashSet<>();
		for (DemandResponseEventReadDto event : existingEvents) {
			if (event.getActivePeriod() != null) {
				existing.add(eventKey(event.getActivePeriod().getStart(), event.getActivePeriod().getDuration()));
			}
		}

		Long end = start.toInstant().toEpochMilli() + nextXHours * 60 * 60 * 1000;

		eventTemplate.forEach(template -> {

			String duration = template.getActivePeriod().getDuration();

			// 원래 log4j 내부 클래스(rolling.action.Duration)를 쓰고 있었다.
			// deprecated 이기도 하고 애초에 로깅 내부용이라 쓸 자리가 아니다.
			// ISO-8601 기간 문자열(PT1H 같은)은 java.time.Duration 이 그대로 읽는다
			Duration parse = Duration.parse(duration);

			long durationMillis = parse.toMillis();

			// 이벤트 칸을 기동 시각(정시)이 아니라 epoch 기준 길이의 배수에 맞춘다.
			// 기동 시각 기준이면 다른 시각에 다시 띄웠을 때 칸이 어긋나서(PT4H 가 1시, 5시 ... 대신 2시, 6시 ...)
			// 이미 있는 이벤트와 겹치는 새 이벤트가 생긴다. 첫 칸은 지금 진행 중인 칸일 수 있다
			long slotStart = Math.floorDiv(start.toInstant().toEpochMilli(), durationMillis) * durationMillis;

			LOGGER.info(String.format("%s %s %s", slotStart, durationMillis, end));

			while (slotStart + durationMillis < end) {

				long eventStart = slotStart;
				slotStart += durationMillis;

				if (existing.contains(eventKey(eventStart, duration))) {
					continue;
				}

				template.getActivePeriod().setStart(eventStart);
				if (template.getBaseline() != null) {
					template.getBaseline().setStart(eventStart);
				}

				try {
					template.setPublished(true);
					demandResponseControllerApi.createUsingPOST(template);

//					demandResponseControllerApi.publishUsingPOST(createUsingPOST.getId());

				} catch (ApiException e) {
					LOGGER.error("Event can't be created", e);
				}

			}

		});

	}

	/**
	 * 이미 있는 이벤트인지 볼 때 쓰는 키. 길이는 문자열 모양(PT1H, PT60M)이 달라도 같게 보도록 밀리초로 바꾼다
	 */
	private static String eventKey(Long start, String duration) {
		long durationMillis;
		try {
			durationMillis = Duration.parse(duration).toMillis();
		} catch (RuntimeException e) {
			// 길이가 없거나 못 읽으면 시작 시각만으로 본다
			durationMillis = -1;
		}
		return start + "/" + durationMillis;
	}

}
