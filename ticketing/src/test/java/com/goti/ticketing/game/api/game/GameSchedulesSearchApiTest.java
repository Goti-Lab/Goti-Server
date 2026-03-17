package com.goti.ticketing.game.api.game;

import com.goti.ticketing.GotiTicketingApplication;
import com.goti.ticketing.constants.LeagueType;
import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.domain.entity.team.BaseballTeamEntity;
import com.goti.ticketing.game.service.application.GameManagementService;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest(classes = GotiTicketingApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("야구 게임 일정 조회 - GET /api/v1/games/schedules")
public class GameSchedulesSearchApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GameManagementService gameManagementService;

	@Autowired
	private BaseballTeamRepository baseballTeamRepository;

	@Autowired
	private StadiumRepository stadiumRepository;

	private BaseballTeamEntity kia;
	private BaseballTeamEntity samsung;
	private StadiumEntity stadium;

	@BeforeEach
	void setup() {
		saveSamsung();
		saveKia();
		saveStadium();

		gameManagementService.register(
			kia.getId(), samsung.getId(), stadium.getId(),
			LocalDateTime.now().plusDays(10).withHour(18).withMinute(30).withSecond(0).withNano(0),
			LeagueType.REGULAR
		);

		gameManagementService.register(
			samsung.getId(), kia.getId(), stadium.getId(),
			LocalDateTime.now().plusDays(11).withHour(14).withMinute(0).withSecond(0).withNano(0),
			LeagueType.REGULAR
		);
	}

	@Test
	@DisplayName("특정 연도와 월로 조회 시 해당 월의 전체 일정 반환 성공")
	void 월간_일정_조회_성공__200_ok() throws Exception {
		int testYear = 2026;
		int testMonth = 5;
		LocalDateTime firstGameDate = LocalDateTime.of(testYear, testMonth, 10, 18, 30);
		LocalDateTime secondGameDate = LocalDateTime.of(testYear, testMonth, 11, 14, 0);

		gameManagementService.register(
			kia.getId(), samsung.getId(), stadium.getId(), firstGameDate, LeagueType.REGULAR
		);
		gameManagementService.register(
			samsung.getId(), kia.getId(), stadium.getId(), secondGameDate, LeagueType.REGULAR
		);

		mockMvc.perform(
				get("/api/v1/games/schedules")
					.with(csrf())
					.param("year", String.valueOf(testYear))
					.param("month", String.valueOf(testMonth))
					.param("today", String.valueOf(false))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2));
	}

	private void saveSamsung() {
		samsung = BaseballTeamEntity.create(
			TeamCode.SS,
			"삼성 라이온즈",
			"Samsung Lions",
			"삼성",
			"대구 삼성 라이온즈 파크",
			1982,
			"주소",
			"54321",
			"url",
			"구단주",
			"단장",
			"감독",
			"기업",
			"대표",
			"logo"
		);
		baseballTeamRepository.save(samsung);
	}

	private void saveKia() {
		kia = BaseballTeamEntity.create(
			TeamCode.KIA,
			"KIA 타이거즈",
			"KIA Tigers",
			"기아",
			"광주-기아 챔피언스 필드",
			1982,
			"주소",
			"12345",
			"url",
			"구단주",
			"단장",
			"감독",
			"기업",
			"대표",
			"logo"
		);
		baseballTeamRepository.save(kia);
	}

	private void saveStadium() {
		stadium = StadiumEntity.create(
			"광주기아챔피언스필드",
			"광주",
			"광주광역시",
			"북구",
			"서림로 10",
			new BigDecimal("35.1"),
			new BigDecimal("126.8"),
			20500,
			Map.of("rows", 50)
		);
		stadiumRepository.save(stadium);
	}
}
