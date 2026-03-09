package com.goti.game.controller;

import com.goti.game.dto.request.CreateGameRequest;
import com.goti.game.dto.response.GameResponse;
import com.goti.game.service.GameScheduleService;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.global.api.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.goti.global.api.ApiSuccessResponse.*;

@Tag(name = "Game Schedule", description = "경기 일정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class GameScheduleController {
	private final GameScheduleService gameScheduleService;

	@Operation(
		summary = "경기 일정 생성",
		description = "야구 경기 일정 생성 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<GameResponse>> create(
		@Valid @RequestBody CreateGameRequest request
	) {
		GameResponse response = gameScheduleService.create(request.toCommand());
		return wrap(response);
	}

	@Operation(
		summary = "경기 일정 전체 조회",
		description = "페이지 기반 야구 경기 일정 조회 API"
	)
	@GetMapping
	public ResponseEntity<ApiSuccessResponse<PageResponse<GameResponse>>> getGames(
		Pageable pageable
	) {
		return page(gameScheduleService.getGames(pageable));
	}

	@Operation(
		summary = "경기 일정 단건 조회",
		description = "야구 경기 일정 상세 조회 API"
	)
	@GetMapping("/{gameId}")
	public ResponseEntity<ApiSuccessResponse<GameResponse>> getGame(
		@PathVariable UUID gameId
	) {
		return wrap(gameScheduleService.getGame(gameId));
	}
}
