package com.goti.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.dto.request.ResaleHoldRequest;
import com.goti.dto.request.ResaleTransactionRequest;
import com.goti.dto.response.ResaleHoldResponse;
import com.goti.dto.response.ResaleReleaseResponse;
import com.goti.dto.response.ResaleTransactionInitResponse;
import com.goti.dto.response.ResaleTransactionSuccessResponse;
import com.goti.global.annotation.LoginUserId;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.application.ResaleHoldService;
import com.goti.service.application.ResaleTransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Resale Transaction", description = "리셀 거래 관련 API")
@RestController
@RequestMapping("/api/v1/resale")
@RequiredArgsConstructor
public class ResaleTransactionController {
	private final ResaleTransactionService transactionService;
	private final ResaleHoldService resaleHoldService;

	@Operation(
		summary = "거래 생성",
		description = "구매자 결제 요청 API"
	)
	@PostMapping("/transactions")
	public ResponseEntity<ApiSuccessResponse<ResaleTransactionInitResponse>> initTransaction(
		@LoginUserId UUID buyerId,
		@Valid @RequestBody ResaleTransactionRequest request
	) {
		ResaleTransactionInitResponse response = transactionService.initTransaction(buyerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 완료 처리",
		description = "결제 완료 전제 기반 리셀 처리 API"
	)
	@PostMapping("/transactions/{transactionId}/complete")
	public ResponseEntity<ApiSuccessResponse<ResaleTransactionSuccessResponse>> completeTransaction(
		@PathVariable UUID transactionId,
		@RequestParam UUID escrowId
	) {
		ResaleTransactionSuccessResponse response = transactionService.completePayment(transactionId, escrowId);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 점유",
		description = "리셀 선점 API"
	)
	@PostMapping("/holds")
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
	@PostMapping("/holds/{holdId}/release")
	public ResponseEntity<ApiSuccessResponse<ResaleReleaseResponse>> releaseResale(
		@LoginUserId UUID buyerId,
		@PathVariable UUID holdId
	) {
		ResaleReleaseResponse response = resaleHoldService.releaseResaleHold(buyerId, holdId);
		return wrap(response);
	}
}