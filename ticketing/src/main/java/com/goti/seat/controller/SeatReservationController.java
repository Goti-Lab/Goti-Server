package com.goti.seat.controller;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.seat.dto.request.HoldSeatRequest;
import com.goti.seat.dto.response.HoldSeatResponse;
import com.goti.seat.dto.response.ReleaseSeatResponse;
import com.goti.seat.service.application.SeatHoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.goti.global.api.ApiSuccessResponse.wrap;

@Tag(name = "Seat Reservation", description = "좌석 점유 및 해제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seat-reservations")
public class SeatReservationController {
	private final SeatHoldService seatHoldService;

	@Operation(
		summary = "좌석 점유",
		description = "좌석을 임시 점유(HOLD) API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<HoldSeatResponse>> hold(
		@RequestParam(required = false) UUID userId, // TODO: 로그인 구현 완료 시 인증 컨텍스트에서 조회
		@Valid @RequestBody HoldSeatRequest request
	) {
		// TODO: 대기열 구현 완료 후 queueTokenJti를 요청값이 아닌 queue token claim(jti)에서 추출하도록 변경
		HoldSeatResponse response = HoldSeatResponse.from(
			seatHoldService.hold(
				request.gameId(),
				request.seatId(),
				userId,
				request.queueTokenJti()
			)
		);
		return wrap(response);
	}

	@Operation(
		summary = "좌석 점유 해제",
		description = "좌석 점유 해제 API"
	)
	@DeleteMapping("/{holdId}")
	public ResponseEntity<ApiSuccessResponse<ReleaseSeatResponse>> release(
		@PathVariable UUID holdId,
		@RequestParam(required = false) UUID userId // TODO: 로그인 구현 완료 시 인증 컨텍스트에서 조회
	) {
		ReleaseSeatResponse response = ReleaseSeatResponse.from(
			seatHoldService.release(holdId, userId)
		);
		return wrap(response);
	}
}
