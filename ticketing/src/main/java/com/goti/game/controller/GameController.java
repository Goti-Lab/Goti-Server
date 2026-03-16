package com.goti.game.controller;

import com.goti.game.dto.request.GameCreateRequest;
import com.goti.game.dto.response.GameCreateResponse;
import com.goti.game.service.application.GameManagementService;

import com.goti.global.api.ApiSuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.goti.global.api.ApiSuccessResponse.wrap;

@Tag(name = "Baseball Game", description = "야구 경기 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class GameController {

	private final GameManagementService gameManagementService;

	@Operation(
		summary = "야구 경기 등록",
		description = "경기 일정 및 상태 정보 등록 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<GameCreateResponse>> create(
		@RequestBody @Valid GameCreateRequest request
	) {
		return wrap(
			gameManagementService.register(
				request.homeTeamId(),
				request.awayTeamId(),
				request.stadiumId(),
				request.startAt(),
				request.leagueType()
			)
		);

	}
}
