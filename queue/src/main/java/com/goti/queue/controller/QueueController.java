package com.goti.queue.controller;

import static com.goti.global.api.ApiSuccessResponse.wrap;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.queue.dto.request.QueueEnterRequest;
import com.goti.queue.dto.response.QueueEnterResponse;
import com.goti.queue.service.QueueEnterService;

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
}
