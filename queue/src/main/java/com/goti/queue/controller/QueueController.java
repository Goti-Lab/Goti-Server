package com.goti.queue.controller;

import static com.goti.global.api.ApiSuccessResponse.wrap;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.constants.messages.SuccessCode;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.queue.dto.request.QueueEnterRequest;
import com.goti.queue.dto.request.QueueSeatEnterRequest;
import com.goti.queue.dto.response.QueueEnterResponse;
import com.goti.queue.dto.response.QueueLeaveResponse;
import com.goti.queue.dto.response.QueueSeatEnterResponse;
import com.goti.queue.dto.response.QueueStatusResponse;
import com.goti.queue.service.QueueEnterService;
import com.goti.queue.service.QueueLeaveService;
import com.goti.queue.service.QueueSeatEnterService;
import com.goti.queue.service.QueueStatusService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Queue", description = "대기열 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class QueueController {

	private final QueueEnterService queueEnterService;
	private final QueueStatusService queueStatusService;
	private final QueueSeatEnterService queueSeatEnterService;
	private final QueueLeaveService queueLeaveService;

	@Operation(
		summary = "대기열 진입",
		description = "경기별 대기열 진입 및 새 대기 순번을 발급 API"
	)
	@PostMapping("/enter")
	public ResponseEntity<ApiSuccessResponse<QueueEnterResponse>> enter(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@Valid @RequestBody QueueEnterRequest request
	) {
		return wrap(queueEnterService.enter(request, userId));
	}

	@Operation(
		summary = "대기열 상태 조회",
		description = "현재 입장 가능 순번 계산을 위한 대기열 메타 정보 조회 API"
	)
	@GetMapping("/{gameId}/status")
	public ResponseEntity<ApiSuccessResponse<QueueStatusResponse>> getStatus(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@PathVariable UUID gameId
	) {
		return wrap(queueStatusService.getStatus(gameId, userId));
	}

	@Operation(
		summary = "대기열 전역 상태 조회 (CDN 캐싱용)",
		description = "인증 불필요. CDN에서 1초 캐싱하여 대량 polling 부하를 흡수."
	)
	@GetMapping("/{gameId}/global-status")
	public ResponseEntity<ApiSuccessResponse<QueueStatusResponse>> getGlobalStatus(
		@PathVariable UUID gameId
	) {
		return ResponseEntity.ok()
			.cacheControl(CacheControl.maxAge(1, TimeUnit.SECONDS).cachePublic())
			.body(new ApiSuccessResponse<>(
				SuccessCode.RESULT.getCode(),
				SuccessCode.RESULT.getMessage(),
				queueStatusService.getGlobalStatus(gameId)
			));
	}

	@Operation(
		summary = "대기열 최종 입장",
		description = "초기 페이지 진입 가능 여부 판단 API"
	)
	@PostMapping("/{gameId}/seat-enter")
	public ResponseEntity<ApiSuccessResponse<QueueSeatEnterResponse>> seatEnter(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@PathVariable UUID gameId,
		@Valid @RequestBody QueueSeatEnterRequest request
	) {
		return wrap(queueSeatEnterService.enter(gameId, userId, request));
	}

	@Operation(
		summary = "대기열 이탈",
		description = "페이지 이탈, 로그아웃, 결제 완료 시 현재 수용 인원 해제 API"
	)
	@PostMapping("/{gameId}/leave")
	public ResponseEntity<ApiSuccessResponse<QueueLeaveResponse>> leave(
		@AuthenticationPrincipal(expression = "id") UUID userId,
		@PathVariable UUID gameId
	) {
		return wrap(queueLeaveService.leave(gameId, userId));
	}
}
