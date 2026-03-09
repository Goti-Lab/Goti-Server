package com.goti.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.dto.request.ResaleHoldRequest;
import com.goti.dto.response.ResaleHoldResponse;
import com.goti.dto.response.ResaleReleaseResponse;
import com.goti.global.annotation.LoginUserId;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.application.ResaleHoldService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Resale Hold", description = "리셀 점유 API")
@RestController
@RequestMapping("/api/v1/resale/holds")
@RequiredArgsConstructor
public class ResaleHoldController {

	private final ResaleHoldService resaleHoldService;

	@Operation(
		summary = "리셀 점유",
		description = "리셀 선점 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<ResaleHoldResponse>> holdResale(
		@LoginUserId UUID buyerId,
		@Valid @RequestBody ResaleHoldRequest request
	) {
		ResaleHoldResponse response = resaleHoldService.holdResale(buyerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 점유 해제",
		description = "리셀 점유 해제 API"
	)
	@DeleteMapping("/{holdId}")
	public ResponseEntity<ApiSuccessResponse<ResaleReleaseResponse>> releaseResale(
		@LoginUserId UUID buyerId,
		@PathVariable UUID holdId
	) {
		ResaleReleaseResponse response = resaleHoldService.releaseResaleHold(buyerId, holdId);
		return wrap(response);
	}
}