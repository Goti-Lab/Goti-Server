package com.goti.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Map;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.GotiStadiumApplication;
import com.goti.constants.StadiumType;
import com.goti.constants.TeamCode;
import com.goti.domain.entity.stadium.StadiumEntity;
import com.goti.domain.entity.team.BaseballTeamEntity;
import com.goti.dto.request.HomeStadiumCreateRequest;
import com.goti.repository.BaseballTeamRepository;
import com.goti.repository.StadiumRepository;

@SpringBootTest(classes = GotiStadiumApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("홈구장 생성 - POST /api/v1/baseball-teams/{teamId}/home-stadiums")
public class HomeStadiumCreateApiTest {

	@Autowired
	public MockMvc mockMvc;

	@Autowired
	public ObjectMapper objectMapper;

	@Autowired
	BaseballTeamRepository baseballTeamRepository;

	@Autowired
	StadiumRepository stadiumRepository;

	BaseballTeamEntity baseballTeam;
	StadiumEntity stadium;

	@BeforeEach
	void setup() {
		saveBaseballTeam();
		saveStadium();
	}

	@Test
	void 홈구장_생성_성공__200_ok() throws Exception {
		HomeStadiumCreateRequest request = new HomeStadiumCreateRequest(
			stadium.getId(),
			StadiumType.PRIMARY
		);

		mockMvc.perform(
				post("/api/v1/baseball-teams/{teamId}/home-stadiums", baseballTeam.getId())
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
				jsonPath("$.data.homeStadiumId").exists(),
				jsonPath("$.data.baseballTeamId").value(baseballTeam.getId().toString()),
				jsonPath("$.data.stadiumId").value(stadium.getId().toString()),
				jsonPath("$.data.teamCode").value(baseballTeam.getTeamCode().name()),
				jsonPath("$.data.teamName").value(baseballTeam.getTeamName()),
				jsonPath("$.data.teamNameEn").value(baseballTeam.getTeamNameEn()),
				jsonPath("$.data.stadiumName").value(stadium.getStadiumName()),
				jsonPath("$.data.location").value(stadium.getLocation()),
				jsonPath("$.data.type").value(StadiumType.PRIMARY.name())
			);
	}

	private void saveBaseballTeam() {
		baseballTeam = BaseballTeamEntity.create(
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