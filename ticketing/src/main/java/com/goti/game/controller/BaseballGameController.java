package com.goti.game.controller;

import com.goti.game.dto.request.CreateGameRequest;
import com.goti.game.dto.response.GameResponse;
import com.goti.game.service.BaseballGameApplicationService;
import com.goti.global.api.ApiSuccessResponse;

import com.goti.global.api.PageResponse;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games/baseball")
public class BaseballGameController {
	private final BaseballGameApplicationService baseballGameApplicationService;

	@PostMapping
	public ResponseEntity<ApiSuccessResponse<GameResponse>> create(
		@Valid @RequestBody CreateGameRequest request
	) {
		GameResponse response = baseballGameApplicationService.create(request.toCommand());
		return wrap(response);
	}

	@GetMapping
	public ResponseEntity<ApiSuccessResponse<PageResponse<GameResponse>>> getGames(
		Pageable pageable
	) {
		return page(baseballGameApplicationService.getGames(pageable));
	}

	@GetMapping("/{gameId}")
	public ResponseEntity<ApiSuccessResponse<GameResponse>> getGame(
		@PathVariable UUID gameId
	) {
		return wrap(baseballGameApplicationService.getGame(gameId));
	}
}
