package com.goti.ticketing.game.service;

import com.goti.ticketing.GotiTicketingApplication;
import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.domain.entity.team.BaseballTeamEntity;
import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.game.dto.request.GameScheduleSearchCondition;
import com.goti.ticketing.game.dto.response.GameScheduleSearchResponse;
import com.goti.ticketing.game.service.application.GameManagementService;
import com.goti.ticketing.game.service.application.GameScheduleSearchService;
import com.goti.stadium.repository.BaseballTeamRepository;
import com.goti.stadium.repository.StadiumRepository;

import com.goti.ticketing.infra.api.StadiumApiClient;

import com.goti.ticketing.infra.api.dto.response.BaseballTeamDisplayNameResponse;

import com.goti.ticketing.infra.api.dto.response.StadiumLocationResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GotiTicketingApplication.class)
@Transactional
@ActiveProfiles("test")
@DisplayName("게임 일정 검색 서비스 테스트")
public class GameScheduleSearchServiceTest {

	@Autowired
	GameScheduleSearchService gameScheduleSearchService;

	@Autowired
	GameManagementService gameManagementService;

	@Autowired
	BaseballTeamRepository baseballTeamRepository;

	@Autowired
	StadiumRepository stadiumRepository;

	@MockitoBean
	private StadiumApiClient StadiumApiClient;

	private BaseballTeamEntity kia;
	private BaseballTeamEntity samsung;
	private StadiumEntity stadium;


	@BeforeEach
	void setUp() {
		saveSamsung();
		saveKia();
		saveStadium();

		when(StadiumApiClient.getBaseballTeamDisplayNames(anyList()))
			.thenReturn(List.of(
				new BaseballTeamDisplayNameResponse(kia.getId(), kia.getDisplayName()),
				new BaseballTeamDisplayNameResponse(samsung.getId(), samsung.getDisplayName())
			));

		when(StadiumApiClient.getStadiumLocations(anyList()))
			.thenReturn(List.of(
				new StadiumLocationResponse(stadium.getId(), stadium.getLocation())
			));

		gameManagementService.register(
			kia.getId(),
			samsung.getId(),
			stadium.getId(),
			LocalDateTime.now().plusDays(10).withHour(18).withMinute(30),
			LeagueType.REGULAR
		);

		gameManagementService.register(
			samsung.getId(),
			kia.getId(),
			stadium.getId(),
			LocalDateTime.now().plusDays(11).withHour(14).withMinute(0),
			LeagueType.REGULAR
		);
	}


	@Test
	@DisplayName("팀 ID 필터를 사용하여 특정 팀의 경기 조회")
	void 팀별_일정_조회_성공() {

		GameScheduleSearchCondition condition = new GameScheduleSearchCondition(
			kia.getId(),
			null,
			null,
			null,
			false
		);

		List<GameScheduleSearchResponse> responses = gameScheduleSearchService.searchSchedules(condition);

		// then
		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).homeTeamDisplayName()).isEqualTo(kia.getDisplayName());
	}

	@Test
	@DisplayName("월간 조회를 통해 10일 뒤와 11일 뒤의 모든 경기 조회")
	void 월간_일정_조회_성공() {
		LocalDateTime targetDate = LocalDateTime.now().plusDays(10);
		GameScheduleSearchCondition condition = new GameScheduleSearchCondition(
			null,
			targetDate.getYear(),
			targetDate.getMonthValue(),
			null,
			false
		);

		List<GameScheduleSearchResponse> responses = gameScheduleSearchService.searchSchedules(condition);

		assertThat(responses).hasSizeGreaterThanOrEqualTo(2);
		assertThat(responses).extracting("homeTeamDisplayName")
			.contains(kia.getDisplayName(), samsung.getDisplayName());
	}

	@Test
	@DisplayName("오늘 일정 조회")
	void 오늘_일정_조회_결과없음() {
		// given
		GameScheduleSearchCondition condition = new GameScheduleSearchCondition(
			null,
			null,
			null,
			null,
			true
		);

		List<GameScheduleSearchResponse> responses = gameScheduleSearchService.searchSchedules(condition);

		assertThat(responses).isEmpty();
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

	private void saveKia() {
		kia = BaseballTeamEntity.create(
			TeamCode.KIA,
			"KIA",
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

	private void saveSamsung() {
		samsung = BaseballTeamEntity.create(
			TeamCode.SS,
			"삼성",
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
}
