package com.goti.ticketing.seat.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.cloudflare.TurnstileService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.seat.dto.request.CreateSeatGradeRequest;
import com.goti.ticketing.seat.dto.request.CreateSeatSectionRequest;
import com.goti.ticketing.seat.dto.response.SeatGradeRegisterResponse;
import com.goti.ticketing.seat.dto.response.SeatGradeSearchResultResponse;
import com.goti.ticketing.seat.dto.response.SeatSectionResponse;
import com.goti.ticketing.seat.service.domain.SeatGradeService;
import com.goti.ticketing.seat.service.domain.SeatSectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@Tag(name = "Seat Grade", description = "구장 별 좌석 등급 및 구역 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stadium-seats")
public class StadiumSeatController {
	private static final String TURNSTILE_TOKEN_HEADER = "X-Turnstile-Token";

	private final SeatGradeService seatGradeService;
	private final SeatSectionService seatSectionService;
	private final TurnstileService turnstileService;

	@Operation(
		summary = "좌석 등급 생성",
		description = "구장별 좌석 등급 생성 API"
	)
	@PostMapping("/seat-grades")
	public ResponseEntity<ApiSuccessResponse<SeatGradeRegisterResponse>> createGrade(
		@Valid @RequestBody CreateSeatGradeRequest request
	) {
		// TODO: 관리자용 API 분리 예정
		SeatGradeRegisterResponse response = seatGradeService.create(
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
	@GetMapping("/games/{gameId}/seat-grades")
	public ResponseEntity<ApiSuccessResponse<SeatGradeSearchResultResponse>> getSeatGrades(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@PathVariable UUID gameId,
		@RequestHeader(name = TURNSTILE_TOKEN_HEADER, required = false) String turnstileToken,
		@RequestParam(defaultValue = "false") boolean forceNewSession
	) {
		log.info("action=TURNSTILE_HEADER_CHECK gameId={} userId={} tokenPresent={} tokenLength={}",
			gameId,
			userId,
			turnstileToken != null && !turnstileToken.isBlank(),
			turnstileToken != null ? turnstileToken.length() : 0
		);
		if (!turnstileService.verify(turnstileToken)) {
			throw new CustomException(ErrorCode.TURNSTILE_VERIFICATION_FAILED);
		}

		return wrap(seatGradeService.findSeatGrades(gameId, userId, forceNewSession));
	}

	@Operation(
		summary = "좌석 구역 생성",
		description = "좌석 등급에 속한 좌석 구역 생성 API"
	)
	@PostMapping("/seat-sections")
	public ResponseEntity<ApiSuccessResponse<SeatSectionResponse>> createSection(
		@Valid @RequestBody CreateSeatSectionRequest request
	) {
		// TODO: 관리자용 API 분리 예정
		SeatSectionResponse response = seatSectionService.create(
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
	@GetMapping("/stadiums/{stadiumId}/seat-sections")
	public ResponseEntity<ApiSuccessResponse<List<SeatSectionResponse>>> getSeatSections(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@PathVariable UUID stadiumId,
		@RequestParam UUID gameId
	) {
		return wrap(seatSectionService.get(stadiumId, gameId, userId));
	}
}
