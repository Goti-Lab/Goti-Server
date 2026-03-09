package com.goti.seat.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.seat.dto.request.BulkCreateSeatsRequest;
import com.goti.seat.dto.response.BulkCreateSeatsResponse;
import com.goti.seat.service.domain.SeatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Seat", description = "좌석 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seats")
public class SeatController {
	private final SeatService seatService;

	@Operation(
		summary = "좌석 일괄 생성",
		description = "좌석 구역의 특정 행에 대한 좌석 번호 범위 일괄 생성 API"
	)
	@PostMapping("/bulk")
	public ResponseEntity<ApiSuccessResponse<BulkCreateSeatsResponse>> createBulk(
		@Valid @RequestBody BulkCreateSeatsRequest request
	) {
		BulkCreateSeatsResponse response = seatService.create(
			request.sectionId(),
			request.rowName(),
			request.startSeatNumber(),
			request.endSeatNumber()
		);
		return wrap(response);
	}
}
