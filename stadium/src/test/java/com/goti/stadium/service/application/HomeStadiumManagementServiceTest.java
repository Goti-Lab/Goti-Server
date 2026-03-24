package com.goti.stadium.service.application;

import com.goti.stadium.GotiStadiumApplication;

import com.goti.stadium.constants.StadiumType;
import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.domain.entity.team.BaseballTeamEntity;

import com.goti.stadium.dto.response.HomeStadiumCreateResponse;
import com.goti.stadium.repository.BaseballTeamRepository;

import com.goti.stadium.repository.StadiumRepository;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = GotiStadiumApplication.class)
@ActiveProfiles("test")
public class HomeStadiumManagementServiceTest {

	@Autowired
	HomeStadiumManagementService homeStadiumManagementService;

	@Autowired
	BaseballTeamRepository baseballTeamRepository;

	@Autowired
	StadiumRepository stadiumRepository;

	BaseballTeamEntity baseballTeam;
	StadiumEntity stadium;
	static final StadiumType STADIUM_TYPE = StadiumType.PRIMARY;

	@BeforeEach
	void setup() {
		saveStadium();
		saveBaseballTeam();
	}

	@Test
	@Transactional
	void 홈구장_생성_성공() {
		HomeStadiumCreateResponse response =
			homeStadiumManagementService.assignHomeStadium(
				baseballTeam.getId(), stadium.getId(), STADIUM_TYPE
			);
		assertNotNull(response);
		assertNotNull(response.homeStadiumId());
		assertEquals(baseballTeam.getId(), response.baseballTeamId());
		assertEquals(baseballTeam.getTeamCode(), response.teamCode());
		assertEquals(baseballTeam.getTeamName(), response.teamName());
		assertEquals(stadium.getId(), response.stadiumId());
		assertEquals(stadium.getStadiumName(), response.stadiumName());
		assertEquals(stadium.getLocation(), response.location());
		assertEquals(STADIUM_TYPE, response.type());
		log.info("homeStadiumId :: {}", response.homeStadiumId());
	}

	private void saveBaseballTeam() {
		baseballTeam = BaseballTeamEntity.create(
			TeamCode.SS,
			"삼성",
			"삼성라이온즈",
			"Samsung Lions",
			"삼성",
			"대구",
			1982,
			"대구광역시 수성구 야구전설로 1",
			"42250",
			"https://www.samsunglions.com/",
			"홍길동",
			"삼성그룹",
			"이재용",
			"이종열",
			"박진만",
			"https://image.url/logo.png"
		);
		baseballTeamRepository.save(baseballTeam);
	}

	private void saveStadium() {
		stadium = StadiumEntity.create(
			"대구삼성라이온즈 파크",
			"대구",
			"대구광역시",
			"수성구",
			"대구광역시 수성구 야구전설로 1",
			new BigDecimal("35.84112000"),
			new BigDecimal("128.68152000"),
			24000,
			Map.of(
				"shape", "octagon",
				"openedYear", 2016,
				"turf", "natural"
			)
		);
		stadiumRepository.save(stadium);
	}

}
