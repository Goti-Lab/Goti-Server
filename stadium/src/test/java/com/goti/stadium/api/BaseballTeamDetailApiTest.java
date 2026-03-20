package com.goti.stadium.api;
import com.goti.stadium.GotiStadiumApplication;

import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.dto.request.BaseballTeamCreateRequest;

import com.goti.stadium.dto.response.BaseballTeamCreateResponse;

import com.goti.stadium.service.domain.baseballteam.BaseballTeamService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest(classes = GotiStadiumApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("야구구단 조회 - GET /api/v1/baseball-teams/{teamId}")
public class BaseballTeamDetailApiTest {

	@Autowired
	public MockMvc mockMvc;

	@Autowired
	BaseballTeamService baseballTeamService;

	UUID teamId;

	@BeforeEach
	void setup() {
		saveBaseballTeam();
	}

	@Test
	void 야구구단_조회_성공__200_OK() throws Exception {
		MvcResult result = mockMvc.perform(
				get("/api/v1/baseball-teams/{teamId}", teamId)
			)
			.andExpectAll(
				status().isOk(),
				jsonPath("$.code").value("ok"),
				jsonPath("$.message").value("성공"),
				jsonPath("$.data").exists(),
				jsonPath("$.data.id").value(String.valueOf(teamId)),
				jsonPath("$.data.teamCode").value(TeamCode.SS.name())
			).andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("response : {}", responseJson);
		log.info("teamId : {}", teamId);
	}

	private void saveBaseballTeam() {
		BaseballTeamCreateRequest request =
			new BaseballTeamCreateRequest(
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
		BaseballTeamCreateResponse response = baseballTeamService.create(
			request.toCommand()
		);
		teamId = response.teamId();
	}
}
