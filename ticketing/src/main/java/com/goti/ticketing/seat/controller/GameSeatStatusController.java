package com.goti.ticketing.seat.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.seat.dto.response.GameSeatStatusInitResponse;
import com.goti.ticketing.seat.dto.response.GameSeatStatusResponse;
import com.goti.ticketing.seat.service.application.GameSeatStatusInitService;
import com.goti.ticketing.seat.service.domain.SeatStatusService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Game Seat Status", description = "경기별 좌석 상태 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game-seats")
public class GameSeatStatusController {
	private final SeatStatusService seatStatusService;
	private final GameSeatStatusInitService gameSeatStatusInitService;

	@Operation(
		summary = "경기 좌석 상태 초기화",
		description = "해당 경기 구장의 모든 좌석에 대해 좌석 상태 AVAILABLE로 생성"
	)
	@PostMapping("/{gameId}/init")
	public ResponseEntity<ApiSuccessResponse<GameSeatStatusInitResponse>> init(
		@PathVariable UUID gameId
	) {
		return wrap(gameSeatStatusInitService.init(gameId));
	}

	@Operation(
		summary = "경기별 좌석 상태 조회",
		description = "특정 경기의 특정 구역 좌석 상태 목록 조회 API"
	)
	@GetMapping("/{gameId}/sections/{sectionId}/seat-statuses")
	public ResponseEntity<ApiSuccessResponse<List<GameSeatStatusResponse>>> list(
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@PathVariable UUID gameId,
		@PathVariable UUID sectionId
	) {
		return wrap(seatStatusService.get(gameId, sectionId, memberId));
	}
}
