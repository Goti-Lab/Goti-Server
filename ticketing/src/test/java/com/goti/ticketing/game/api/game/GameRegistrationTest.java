package com.goti.ticketing.game.api.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.ticketing.GotiTicketingApplication;
import com.goti.ticketing.constants.LeagueType;
import com.goti.stadium.constants.TeamCode;
import com.goti.ticketing.constants.TicketingStatus;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;

import com.goti.stadium.domain.entity.team.BaseballTeamEntity;

import com.goti.ticketing.game.dto.request.GameCreateRequest;
import com.goti.stadium.repository.BaseballTeamRepository;

import com.goti.stadium.repository.StadiumRepository;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Slf4j
@SpringBootTest(classes = GotiTicketingApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("야구 경기 등록 - POST /api/v1/games")
@AutoConfigureWireMock(port = 8080)
public class GameRegistrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	BaseballTeamRepository baseballTeamRepository;

	@Autowired
	StadiumRepository stadiumRepository;

	BaseballTeamEntity homeTeam;
	BaseballTeamEntity awayTeam;
	StadiumEntity stadium;

	private static final LocalDateTime START_AT =
		LocalDateTime.now().plusDays(3).withMinute(30).withSecond(0).withNano(0);
	private static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;
	private static final int TICKETING_START_HOUR = 11;

	private static final String BASEBALL_GET_API_URI = "/api/v1/baseball-teams/";
	private static final String STADIUM_GET_API_URI = "/api/v1/stadiums/";

	@BeforeEach
	void setup() {
		saveAwayTeam();
		saveHomeTeam();
		saveStadium();

		stubFor(
			get(
				urlEqualTo(BASEBALL_GET_API_URI + homeTeam.getId())
			).willReturn(aResponse().withStatus(200))
		);

		stubFor(
			get(
				urlEqualTo(BASEBALL_GET_API_URI + awayTeam.getId())
			).willReturn(aResponse().withStatus(200))
		);

		stubFor(
			get(
				urlEqualTo(STADIUM_GET_API_URI + stadium.getId())
			).willReturn(aResponse().withStatus(200))
		);
	}


	@Test
	void 야구_경기_등록_성공__200_ok() throws Exception {

		GameCreateRequest request = new GameCreateRequest(
			homeTeam.getId(),
			awayTeam.getId(),
			stadium.getId(),
			START_AT,
			LEAGUE_TYPE
		);

		LocalDateTime executionTime = LocalDateTime.now();

		LocalDateTime expectedOpenTime = executionTime.toLocalDate().atTime(TICKETING_START_HOUR, 0);
		String formattedOpenedAt = expectedOpenTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

		String formattedEndAt = START_AT.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

		TicketingStatus expectedStatus = TicketingStatus.SCHEDULED;
		if (expectedOpenTime.isBefore(executionTime) || expectedOpenTime.isEqual(executionTime)) {
			expectedStatus = TicketingStatus.AVAILABLE;
		}

		MvcResult result = mockMvc.perform(
				post("/api/v1/games")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andDo(print())
			.andExpectAll(
				status().isOk(),
				jsonPath("$.code").value("ok"),
				jsonPath("$.message").value("성공"),
				jsonPath("$.data").exists(),
				jsonPath("$.data.gameId").exists(),
				jsonPath("$.data.homeTeamId").value(homeTeam.getId().toString()),
				jsonPath("$.data.awayTeamId").value(awayTeam.getId().toString()),
				jsonPath("$.data.stadiumId").value(stadium.getId().toString()),
				jsonPath("$.data.ticketingOpenedAt").value(formattedOpenedAt),
				jsonPath("$.data.ticketingEndAt").value(formattedEndAt),
				jsonPath("$.data.ticketingStatus").value(expectedStatus.name())

			).andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("response : {}", responseJson);
	}

	void saveHomeTeam() {
		homeTeam = BaseballTeamEntity.create(
			TeamCode.KIA,
			"KIA 타이거즈",
			"KIA Tigers",
			"기아",
			"광주-기아 챔피언스 필드",
			1982,
			"광주광역시 북구 서림로 10",
			"61257",
			"https://www.kiatigers.co.kr",
			"정의선",
			"심재학",
			"이범호",
			"기아",
			"최준영",
			"https://example.com/logos/kia.png"
		);
		baseballTeamRepository.save(homeTeam);
	}

	void saveAwayTeam() {
		awayTeam = BaseballTeamEntity.create(
			TeamCode.SS,
			"삼성 라이온즈",
			"Samsung Lions",
			"삼성",
			"대구 삼성 라이온즈 파크",
			1982,
			"대구광역시 수성구 야구전설로 1",
			"42250",
			"https://www.samsunglions.com",
			"이재용",
			"이종열",
			"박진만",
			"삼성레저",
			"유정희",
			"https://example.com/logos/samsung.png"
		);
		baseballTeamRepository.save(awayTeam);
	}

	void saveStadium() {
		Map<String, Object> kiaSeatConfig = Map.of(
			"rows", 50,
			"sections", List.of("K3", "K5", "K7", "K9", "챔피언석")
		);

		stadium = StadiumEntity.create(
			"광주-기아 챔피언스 필드",
			"광주광역시 북구 임동",
			"광주광역시",
			"북구",
			"광주광역시 북구 서림로 10",
			new BigDecimal("35.1681"),
			new BigDecimal("126.8894"),
			20500,
			kiaSeatConfig
		);

		stadiumRepository.save(stadium);
	}

}
