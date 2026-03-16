package com.goti.stadium.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.stadium.GotiStadiumApplication;
import com.goti.stadium.config.TestSecurityConfig;
import com.goti.stadium.dto.request.StadiumCreateRequest;

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

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Import(TestSecurityConfig.class)
@SpringBootTest(classes = GotiStadiumApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("야구 구장 생성 - POST /api/v1/stadiums")
public class StadiumCreateApiTest {

	@Autowired
	public MockMvc mockMvc;

	@Autowired
	public ObjectMapper objectMapper;

	static final String STADIUM_NAME = "대구삼성라이온즈파크";
	static final String ROAD_ADDRESS = "대구 수성구 야구전설로 1 대구삼성라이온즈파크";

	@Test
	void 야구구장_생성_성공__200_OK() throws Exception {
		Map<String, Object> seatMapConfig = Map.of("sections", "test");
		StadiumCreateRequest request = new StadiumCreateRequest(
			STADIUM_NAME,
			"대구",
			"대구광역시",
			"수성구",
			ROAD_ADDRESS,
			BigDecimal.valueOf(35.84),
			BigDecimal.valueOf(128.68),
			24000,
			seatMapConfig
		);

		mockMvc.perform(
			post("/api/v1/stadiums")
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
				jsonPath("$.data.stadiumId").exists(),
				jsonPath("$.data.stadiumName").value(STADIUM_NAME),
				jsonPath("$.data.roadAddress").value(ROAD_ADDRESS)
			);
	}
}
