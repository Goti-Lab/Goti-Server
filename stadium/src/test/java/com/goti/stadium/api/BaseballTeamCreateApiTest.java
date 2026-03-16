package com.goti.stadium.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.stadium.GotiStadiumApplication;

import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.dto.request.BaseballTeamCreateRequest;

import com.goti.stadium.config.TestSecurityConfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Import(TestSecurityConfig.class)
@SpringBootTest(classes = GotiStadiumApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("야구 구단 생성 - POST /api/v1/baseball-teams")
public class BaseballTeamCreateApiTest {
	@Autowired
	public MockMvc mockMvc;

	@Autowired
	public ObjectMapper objectMapper;

	@Test
	void 야구구단_생성_성공__200_ok() throws Exception {

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

		mockMvc.perform(
				post("/api/v1/baseball-teams")
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
				jsonPath("$.data.teamId").exists(),
				jsonPath("$.data.teamCode").value(request.teamCode().toString()),
				jsonPath("$.data.teamName").value(request.teamName()),
				jsonPath("$.data.teamNameEn").value(request.teamNameEn()),
				jsonPath("$.data.homeGround").value(request.homeGround()),
				jsonPath("$.data.owner").value(request.owner()),
				jsonPath("$.data.director").value(request.director())
			);
	}

}
