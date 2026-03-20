package com.goti.stadium.api;

import com.goti.stadium.GotiStadiumApplication;
import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.repository.StadiumRepository;
import com.goti.stadium.service.domain.baseballteam.BaseballTeamService;

import com.goti.stadium.service.domain.stadium.StadiumService;

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

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest(classes = GotiStadiumApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("야구구장 조회 - GET /api/v1/stadiums/{stadiumId}")
public class StadiumDetailApiTest {

	@Autowired
	public MockMvc mockMvc;

	@Autowired
	StadiumRepository stadiumRepository;

	StadiumEntity stadium;

	static final String STADIUM_NAME = "대구삼성라이온즈파크";

	@BeforeEach
	void setup() {
		saveStadium();
	}

	@Test
	void 야구구장_조회_성공__200_OK() throws Exception {
		MvcResult result = mockMvc.perform(
				get("/api/v1/stadiums/{stadiumId}", stadium.getId())
			)
			.andExpectAll(
				status().isOk(),
				jsonPath("$.code").value("ok"),
				jsonPath("$.message").value("성공"),
				jsonPath("$.data").exists(),
				jsonPath("$.data.id").exists(),
				jsonPath("$.data.id").value(String.valueOf(stadium.getId())),
				jsonPath("$.data.stadiumName").value(STADIUM_NAME)
			).andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("response : {}", responseJson);
		log.info("stadiumId : {}", stadium.getId());
	}

	private void saveStadium() {
		stadium = StadiumEntity.create(
			"대구삼성라이온즈파크",
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
