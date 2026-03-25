package com.goti.controller;

import com.goti.dto.request.QueueValidateRequest;
import com.goti.dto.response.QueueStatusResponse;
import com.goti.dto.response.QueueValidateResponse;
import com.goti.global.api.ApiSuccessResponse;

import com.goti.service.QueueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.goti.global.api.ApiSuccessResponse.*;

@Slf4j
@Tag(name = "Waiting-Queue", description = "대기열 관련 API")
@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class QueueController {

	private final QueueService queueService;

	@Operation(
		summary = "대기열 진입 가능 여부 검증",
		description = "대기열 "
	)
	@PostMapping("/validate")
	public ResponseEntity<ApiSuccessResponse<QueueValidateResponse>> create(
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@RequestBody @Valid QueueValidateRequest request
	) {
		return wrap(
			queueService.validate(request.gameId(), memberId)
		);
	}

	@Operation(
		summary = "대기열 상태 조회 (Polling)",
		description = "대기 중인 유저가 자신의 순번이나 통과 여부를 확인하는 API"
	)
	@GetMapping("/status/games/{gameId}")
	public ResponseEntity<ApiSuccessResponse<QueueStatusResponse>> getStatus(
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@PathVariable UUID gameId
	) {
		return wrap(queueService.getStatus(gameId, memberId));
	}




}
