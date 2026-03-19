package com.goti.ticketing.seat.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.seat.dto.response.SeatSectionRegisterResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.seat.dto.request.CreateSeatGradeRequest;
import com.goti.ticketing.seat.dto.request.CreateSeatSectionRequest;
import com.goti.ticketing.seat.dto.response.SeatGradeResponse;
import com.goti.ticketing.seat.dto.response.SeatSectionSearchResponse;
import com.goti.ticketing.seat.service.domain.SeatGradeService;
import com.goti.ticketing.seat.service.domain.SeatSectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Seat Grade", description = "구장 별 좌석 등급 및 구역 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stadium-seats")
public class StadiumSeatController {
	private final SeatGradeService seatGradeService;
	private final SeatSectionService seatSectionService;

	@Operation(
		summary = "좌석 등급 생성",
		description = "구장별 좌석 등급 생성 API"
	)
	@PostMapping("/seat-grades")
	public ResponseEntity<ApiSuccessResponse<SeatGradeResponse>> createGrade(
		@Valid @RequestBody CreateSeatGradeRequest request
	) {
		// TODO: 관리자용 API 분리 예정
		SeatGradeResponse response = seatGradeService.create(
			request.stadiumId(),
			request.name(),
			request.displayColorHex()
		);
		return wrap(response);
	}

	@Operation(
		summary = "좌석 등급 조회",
		description = "구장별 좌석 등급 조회 API"
	)
	@GetMapping("/stadiums/{stadiumId}/seat-grades")
	public ResponseEntity<ApiSuccessResponse<List<SeatGradeResponse>>> getSeatGrades(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@PathVariable UUID stadiumId
	) {
		return wrap(seatGradeService.get(stadiumId, userId));
	}

	@Operation(
		summary = "좌석 구역 생성",
		description = "좌석 등급에 속한 좌석 구역 생성 API"
	)
	@PostMapping("/seat-sections")
	public ResponseEntity<ApiSuccessResponse<SeatSectionRegisterResponse>> createSection(
		@Valid @RequestBody CreateSeatSectionRequest request
	) {
		// TODO: 관리자용 API 분리 예정
		SeatSectionRegisterResponse response = seatSectionService.create(
			request.gradeId(),
			request.stadiumId(),
			request.sectionCode(),
			request.capacity()
		);
		return wrap(response);
	}

	@Operation(
		summary = "좌석 구역 조회",
		description = "구장별 좌석 구역 목록 조회 API"
	)
	@GetMapping("/stadiums/{stadiumId}/games/{gameId}/seat-sections")
	public ResponseEntity<ApiSuccessResponse<List<SeatSectionSearchResponse>>> getSeatSections(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@PathVariable UUID stadiumId,
		@PathVariable UUID gameId
	) {
		return wrap(seatSectionService.get(stadiumId, userId, gameId));
	}
}
