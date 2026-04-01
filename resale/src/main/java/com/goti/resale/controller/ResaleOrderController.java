package com.goti.resale.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.resale.dto.request.ResaleHoldRequest;
import com.goti.resale.dto.request.ResaleOrderRequest;
import com.goti.resale.dto.request.ResaleOrderPeriodFilterRequest;
import com.goti.resale.dto.response.ResaleHoldResponse;
import com.goti.resale.dto.response.ResaleOrderCompleteResponse;
import com.goti.resale.dto.response.ResaleOrderCreateResponse;
import com.goti.resale.dto.response.ResaleOrderListResponse;
import com.goti.resale.dto.response.ResalePurchaseListResponse;
import com.goti.resale.dto.response.ResaleReleaseResponse;
import com.goti.resale.service.application.ResaleHoldProcessService;
import com.goti.resale.service.application.ResaleOrderProcessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Resale Order", description = "리셀 주문 및 거래 관련 API")
@RestController
@RequestMapping("/api/v1/resales")
@RequiredArgsConstructor
public class ResaleOrderController {
	private final ResaleOrderProcessService resaleOrderProcessService;
	private final ResaleHoldProcessService resaleHoldProcessService;

	@Operation(
		summary = "리셀 주문 생성",
		description = "구매자 결제 요청을 위한 리셀 주문 생성 API"
	)
	@PostMapping("/orders")
	public ResponseEntity<ApiSuccessResponse<ResaleOrderCreateResponse>> createOrder(
		@AuthenticationPrincipal(expression = "id") UUID buyerId,
		@Valid @RequestBody ResaleOrderRequest request
	) {
		ResaleOrderCreateResponse response = resaleOrderProcessService.initOrder(buyerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 주문 완료 처리",
		description = "결제 완료 전제 기반 리셀 주문 처리 API"
	)
	@PatchMapping("/orders/{resaleOrderId}/complete")
	public ResponseEntity<ApiSuccessResponse<ResaleOrderCompleteResponse>> completeOrder(
		@PathVariable UUID resaleOrderId,
		@RequestParam UUID paymentId
	) {
		ResaleOrderCompleteResponse response = resaleOrderProcessService.completePayment(resaleOrderId, paymentId);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 정산 최종 완료 처리",
		description = "실제 은행 송금이 완료 및 정산 완료 처리 API"
	)
	@PatchMapping("/orders/{resaleOrderId}/settled")
	public ResponseEntity<ApiSuccessResponse<Void>> completeSettlement(
		@PathVariable UUID resaleOrderId
	) {
		resaleOrderProcessService.completeSettlement(resaleOrderId);
		return empty();
	}

	@Operation(
		summary = "주문별 거래 ID 목록 조회",
		description = "특정 리셀 주문에 포함된 거래 ID 목록을 조회 API"
	)
	@GetMapping("/orders/{resaleOrderId}/transactions")
	public ResponseEntity<ApiSuccessResponse<ResaleOrderListResponse>> getTransactionIds(
		@PathVariable UUID resaleOrderId
	) {
		return wrap(resaleOrderProcessService.getTransactionIds(resaleOrderId));
	}

	@Operation(
		summary = "내 리셀 구매 내역 조회 (내부용)",
		description = "payment 모듈에서 리셀 구매 내역 목록 조회 API"
	)
	@GetMapping("/orders/purchases")
	public ResponseEntity<ApiSuccessResponse<List<ResalePurchaseListResponse>>> getPurchasesByMember(
		@RequestParam UUID buyerId,
		@ParameterObject ResaleOrderPeriodFilterRequest request
	) {
		return wrap(
			resaleOrderProcessService.getPurchasesByMember(
			buyerId,
			request.months(),
			request.startDate(),
			request.endDate()
		));
	}

	@Operation(
		summary = "리셀 점유",
		description = "리셀 선점 API"
	)
	@PostMapping("/holds")
	public ResponseEntity<ApiSuccessResponse<ResaleHoldResponse>> holdResale(
		@AuthenticationPrincipal(expression = "id") UUID buyerId,
		@Valid @RequestBody ResaleHoldRequest request
	) {
		ResaleHoldResponse response = resaleHoldProcessService.holdResale(buyerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 점유 해제",
		description = "리셀 점유 해제 API"
	)
	@PatchMapping("/holds/{holdId}/release")
	public ResponseEntity<ApiSuccessResponse<ResaleReleaseResponse>> releaseResale(
		@AuthenticationPrincipal(expression = "id") UUID buyerId,
		@PathVariable UUID holdId
	) {
		ResaleReleaseResponse response = resaleHoldProcessService.releaseResaleHold(buyerId, holdId);
		return wrap(response);
	}
}