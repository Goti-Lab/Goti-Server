package com.goti.ticketing.seat.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.seat.dto.request.HoldSeatRequest;
import com.goti.ticketing.seat.dto.response.HoldSeatResponse;
import com.goti.ticketing.seat.dto.response.ReleaseSeatResponse;
import com.goti.ticketing.seat.service.application.SeatHoldManageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Seat Reservation", description = "좌석 점유 및 해제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seat-reservations")
public class SeatReservationController {
	private final SeatHoldManageService seatHoldManageService;

	@Operation(
		summary = "좌석 점유",
		description = "좌석 임시 점유(HOLD) API"
	)
	@PostMapping("/seats/{seatId}")
	public ResponseEntity<ApiSuccessResponse<HoldSeatResponse>> hold(
		@PathVariable UUID seatId,
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@Valid @RequestBody HoldSeatRequest request
	) {
		// TODO: 대기열 구현 완료 후 queueTokenJti를 요청값이 아닌 queue token claim(jti)에서 추출하도록 변경
		UUID holdId = seatHoldManageService.hold(
			request.gameId(),
			seatId,
			memberId,
			request.queueTokenJti()
		);
		HoldSeatResponse response = HoldSeatResponse.from(holdId);
		return wrap(response);
	}

	@Operation(
		summary = "좌석 점유 해제",
		description = "좌석 점유 해제 API"
	)
	@PostMapping("/{holdId}")
	public ResponseEntity<ApiSuccessResponse<ReleaseSeatResponse>> release(
		@PathVariable UUID holdId,
		@AuthenticationPrincipal(expression = "id") UUID memberId
	) {
		UUID releasedHoldId = seatHoldManageService.release(holdId, memberId);
		ReleaseSeatResponse response = ReleaseSeatResponse.from(releasedHoldId);
		return wrap(response);
	}
}
