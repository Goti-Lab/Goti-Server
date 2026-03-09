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

import com.goti.dto.request.ResaleTransactionRequest;
import com.goti.dto.response.ResaleTransactionInitResponse;
import com.goti.dto.response.ResaleTransactionSuccessResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.ResaleTransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "리셀 거래", description = "리셀 거래 API")
@RestController
@RequestMapping("/api/v1/resale/transactions")
@RequiredArgsConstructor
public class ResaleTransactionController {
	private final ResaleTransactionService transactionService;

	@Operation(
		summary = "거래 생성",
		description = "구매자 결제 요청 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<ResaleTransactionInitResponse>> initTransaction(
		@RequestParam(required = false) UUID buyerId, // TODO : 로그인 구현완료시 로그인으로 받아올 것
		@Valid @RequestBody ResaleTransactionRequest request
	) {
		ResaleTransactionInitResponse response = transactionService.initTransaction(buyerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 완료 처리",
		description = "결제 완료 전제 기반 리셀 처리 API"
	)
	@PostMapping("/{transactionId}/complete")
	public ResponseEntity<ApiSuccessResponse<ResaleTransactionSuccessResponse>> completeTransaction(
		@PathVariable UUID transactionId,
		@RequestParam UUID escrowId
	) {
		ResaleTransactionSuccessResponse response = transactionService.completePayment(transactionId, escrowId);
		System.out.println();
		return wrap(response);
	}

}
