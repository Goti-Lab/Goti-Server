package com.goti.ticketing.game.service;

import com.goti.GotiTicketingApplication;

import com.goti.ticketing.constants.LeagueType;
import com.goti.constants.TeamCode;
import com.goti.ticketing.constants.TicketingStatus;
import com.goti.domain.entity.stadium.StadiumEntity;
import com.goti.domain.entity.team.BaseballTeamEntity;
import com.goti.ticketing.game.dto.response.GameCreateResponse;
import com.goti.ticketing.game.service.application.GameManagementService;

import com.goti.repository.BaseballTeamRepository;

import com.goti.repository.StadiumRepository;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest(classes = GotiTicketingApplication.class)
@ActiveProfiles("test")
public class GameManagementServiceTest {

	@Autowired
	GameManagementService gameManagementService;

	@Autowired
	BaseballTeamRepository baseballTeamRepository;

	@Autowired
	StadiumRepository stadiumRepository;

	BaseballTeamEntity homeTeam;
	BaseballTeamEntity awayTeam;
	StadiumEntity stadium;

	static final LocalDateTime START_AT =
		LocalDateTime.now().plusDays(3).withHour(18).withMinute(30).withSecond(0).withNano(0);

	static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;

	@BeforeEach
	void setup() {
		saveHomeTeam();
		saveAwayTeam();
		saveStadium();
	}

	@Test
	@DisplayName("method: create() - 경기 생성 성공")
	void 경기_생성_성공() {
		GameCreateResponse response = gameManagementService.register(
			homeTeam.getId(),
			awayTeam.getId(),
			stadium.getId(),
			START_AT,
			LEAGUE_TYPE
		);

		LocalDateTime executionTime = LocalDateTime.now();

		LocalDateTime expectedOpenAt = executionTime.toLocalDate().atTime(11, 0);

		TicketingStatus expectedStatus = TicketingStatus.SCHEDULED;
		if (expectedOpenAt.isBefore(executionTime) || expectedOpenAt.isEqual(executionTime)) {
			expectedStatus = TicketingStatus.AVAILABLE;
		}

		assertNotNull(response.gameId());
		assertNotNull(response.ticketingOpenedAt());
		assertNotNull(response.ticketingEndAt());
		assertEquals(expectedStatus, response.ticketingStatus());
		log.info("response gameId : {}", response.gameId());
		log.info("response ticketingStatus :: {}", response.ticketingStatus());
		log.info("response ticketingOpenedAt :: {}", response.ticketingOpenedAt());
		log.info("response ticketingEndAt :: {}", response.ticketingEndAt());
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
