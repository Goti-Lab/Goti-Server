package com.goti.game.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.constants.GameResult;
import com.goti.constants.GameStatus;
import com.goti.constants.LeagueType;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.exception.handler.SpringExceptionHandler;
import com.goti.exception.handler.SystemExceptionHandler;
import com.goti.game.dto.request.CreateGameRequest;
import com.goti.game.dto.response.GameResponse;
import com.goti.game.service.BaseballGameApplicationService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@ActiveProfiles("test")
@WebMvcTest(BaseballGameController.class)
@ContextConfiguration(classes = {
	BaseballGameController.class,
	SystemExceptionHandler.class,
	SpringExceptionHandler.class
})
class BaseballGameControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private BaseballGameApplicationService baseballGameApplicationService;

	@Test
	@DisplayName("POST /api/v1/games/baseball - 경기 생성 성공")
	@WithMockUser
	void 경기_생성_API_성공() throws Exception {
		UUID gameId = UUID.randomUUID();
		UUID homeTeamId = UUID.randomUUID();
		UUID awayTeamId = UUID.randomUUID();
		UUID stadiumId = UUID.randomUUID();

		CreateGameRequest request = new CreateGameRequest(
			homeTeamId,
			awayTeamId,
			stadiumId,
			LocalDate.of(2026, 4, 10),
			LocalTime.of(18, 30),
			LeagueType.REGULAR
		);

		GameResponse response = new GameResponse(
			gameId,
			homeTeamId,
			awayTeamId,
			stadiumId,
			request.playDate(),
			request.startAt(),
			LeagueType.REGULAR,
			GameStatus.SCHEDULED,
			0,
			0,
			GameResult.PENDING
		);
		given(baseballGameApplicationService.create(any())).willReturn(response);

		mockMvc.perform(
				post("/api/v1/games/baseball")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("ok"))
			.andExpect(jsonPath("$.message").value("성공"))
			.andExpect(jsonPath("$.data.gameId").value(gameId.toString()))
			.andExpect(jsonPath("$.data.homeTeamId").value(homeTeamId.toString()));
	}

	@Test
	@DisplayName("POST /api/v1/games/baseball - 필수값 누락 시 400 반환")
	@WithMockUser
	void 경기_생성_API_실패_필수값_누락() throws Exception {
		String invalidBody = """
			{
			  "homeTeamId": "%s",
			  "awayTeamId": "%s",
			  "stadiumId": "%s",
			  "playDate": "2026-04-10",
			  "startAt": "18:30:00"
			}
			""".formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

		mockMvc.perform(
				post("/api/v1/games/baseball")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidBody)
			)
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /api/v1/games/baseball - 경기 전체 조회 성공")
	@WithMockUser
	void 경기_전체조회_API_성공() throws Exception {
		UUID gameId = UUID.randomUUID();
		GameResponse response = new GameResponse(
			gameId,
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDate.of(2026, 4, 10),
			LocalTime.of(18, 30),
			LeagueType.REGULAR,
			GameStatus.SCHEDULED,
			0,
			0,
			GameResult.PENDING
		);
		Page<GameResponse> page = new PageImpl<>(
			java.util.List.of(response),
			PageRequest.of(0, 10),
			1
		);
		given(baseballGameApplicationService.getGames(any())).willReturn(page);

		mockMvc.perform(
				get("/api/v1/games/baseball")
					.param("page", "0")
					.param("size", "10")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("ok"))
			.andExpect(jsonPath("$.message").value("성공"))
			.andExpect(jsonPath("$.data.list[0].gameId").value(gameId.toString()))
			.andExpect(jsonPath("$.data.totalCount").value(1))
			.andExpect(jsonPath("$.data.totalPages").value(1));
	}

	@Test
	@DisplayName("GET /api/v1/games/baseball/{gameId} - 경기 단건 조회 성공")
	@WithMockUser
	void 경기_단건조회_API_성공() throws Exception {
		UUID gameId = UUID.randomUUID();
		GameResponse response = new GameResponse(
			gameId,
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDate.of(2026, 4, 10),
			LocalTime.of(18, 30),
			LeagueType.REGULAR,
			GameStatus.SCHEDULED,
			0,
			0,
			GameResult.PENDING
		);
		given(baseballGameApplicationService.getGame(gameId)).willReturn(response);

		mockMvc.perform(get("/api/v1/games/baseball/{gameId}", gameId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("ok"))
			.andExpect(jsonPath("$.message").value("성공"))
			.andExpect(jsonPath("$.data.gameId").value(gameId.toString()));
	}

	@Test
	@DisplayName("GET /api/v1/games/baseball/{gameId} - 경기 미존재 시 404 반환")
	@WithMockUser
	void 경기_단건조회_API_실패_미존재() throws Exception {
		UUID gameId = UUID.randomUUID();
		given(baseballGameApplicationService.getGame(gameId))
			.willThrow(new CustomException(ErrorCode.GAME_NOT_FOUND));

		mockMvc.perform(get("/api/v1/games/baseball/{gameId}", gameId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("CLIENT_ERROR"))
			.andExpect(jsonPath("$.message").value(ErrorCode.GAME_NOT_FOUND.getMessage()));
	}
}
