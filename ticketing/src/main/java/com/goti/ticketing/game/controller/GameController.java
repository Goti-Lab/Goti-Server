package com.goti.ticketing.game.controller;

import com.goti.ticketing.game.dto.request.GameCreateRequest;
import com.goti.ticketing.game.dto.request.GameScheduleSearchCondition;
import com.goti.ticketing.game.dto.response.GameCreateResponse;
import com.goti.ticketing.game.dto.response.GameScheduleSearchResponse;
import com.goti.ticketing.game.service.application.GameManagementService;
import com.goti.ticketing.game.service.application.GameScheduleSearchService;
import com.goti.global.api.ApiSuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.goti.global.api.ApiSuccessResponse.wrap;

@Tag(name = "Baseball Game", description = "야구 경기 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class GameController {

	private final GameManagementService gameManagementService;
	private final GameScheduleSearchService scheduleSearchService;

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

	@Operation(
		summary = "야구 경기 일정 조회",
		description = "야구 경기 일정 (당일, 일정별, 팀별) 조회 API"
	)
	@GetMapping("/schedules")
	public ResponseEntity<ApiSuccessResponse<List<GameScheduleSearchResponse>>> search(
		@Valid GameScheduleSearchCondition condition
	) {
		return wrap(
			scheduleSearchService.searchSchedules(condition)
		);
	}

}
