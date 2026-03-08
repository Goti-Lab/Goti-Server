package com.goti.seat.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.seat.dto.request.CreateSeatGradeRequest;
import com.goti.seat.dto.response.SeatGradeResponse;
import com.goti.seat.service.application.SeatGradeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Seat Grade", description = "좌석 등급 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seat-grades")
public class SeatGradeController {
	private final SeatGradeService seatGradeService;

	@Operation(
		summary = "좌석 등급 생성",
		description = "구장별 좌석 등급을 생성하는 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<SeatGradeResponse>> create(
		@Valid @RequestBody CreateSeatGradeRequest request
	) {
		SeatGradeResponse response = seatGradeService.create(
			request.stadiumId(),
			request.name(),
			request.displayColorHex()
		);
		return wrap(response);
	}

	@Operation(
		summary = "좌석 등급 조회",
		description = "구장별 좌석 등급을 조회하는 API"
	)
	@GetMapping
	public ResponseEntity<ApiSuccessResponse<List<SeatGradeResponse>>> get( // TODO: 유저 인증 추가
		@RequestParam UUID stadiumId
	) {
		return wrap(seatGradeService.get(stadiumId));
	}
}
