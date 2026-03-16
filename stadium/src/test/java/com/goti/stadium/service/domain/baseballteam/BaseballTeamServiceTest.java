package com.goti.stadium.service.domain.baseballteam;

import com.goti.stadium.GotiStadiumApplication;

import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.dto.request.BaseballTeamCreateRequest;
import com.goti.stadium.dto.response.BaseballTeamCreateResponse;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@Transactional
@SpringBootTest(classes = GotiStadiumApplication.class)
@ActiveProfiles("test")
public class BaseballTeamServiceTest {

	@Autowired
	BaseballTeamService baseballTeamService;

	BaseballTeamCreateRequest baseballTeamCreateRequest;

	@BeforeEach
	void setup() {
		baseballTeamCreateRequest = new BaseballTeamCreateRequest(
			TeamCode.SS,
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
	}

	@Test
	void 야구구단_생성_성공() {

		BaseballTeamCreateResponse response = baseballTeamService.create(
			baseballTeamCreateRequest.toCommand()
		);

		assertNotNull(response.teamId());
		log.info("response teamId :: {}", response.teamId());
		log.info("response teamName :: {}", response.teamName());

	}

}
