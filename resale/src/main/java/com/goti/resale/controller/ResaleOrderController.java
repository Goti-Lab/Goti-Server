package com.goti.resale.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.resale.dto.request.ResaleHoldRequest;
import com.goti.resale.dto.request.ResaleOrderRequest;
import com.goti.resale.dto.response.ResaleHoldResponse;
import com.goti.resale.dto.response.ResaleOrderCompleteResponse;
import com.goti.resale.dto.response.ResaleOrderCreateResponse;
import com.goti.resale.dto.response.ResaleReleaseResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.resale.service.application.ResaleHoldService;
import com.goti.resale.service.application.ResaleOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Resale Order", description = "리셀 주문 및 거래 관련 API")
@RestController
@RequestMapping("/api/v1/resale")
@RequiredArgsConstructor
public class ResaleOrderController {
	private final ResaleOrderService resaleOrderService;
	private final ResaleHoldService resaleHoldService;

	@Operation(
		summary = "리셀 주문 생성",
		description = "구매자 결제 요청을 위한 리셀 주문 생성 API"
	)
	@PostMapping("/orders")
	public ResponseEntity<ApiSuccessResponse<ResaleOrderCreateResponse>> createOrder(
		@AuthenticationPrincipal(expression = "id") UUID buyerId,
		@Valid @RequestBody ResaleOrderRequest request
	) {
		ResaleOrderCreateResponse response = resaleOrderService.initOrder(buyerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 주문 완료 처리",
		description = "결제 완료 전제 기반 리셀 주문 처리 API"
	)
	@PostMapping("/orders/{resaleOrderId}/complete")
	public ResponseEntity<ApiSuccessResponse<ResaleOrderCompleteResponse>> completeOrder(
		@PathVariable UUID resaleOrderId,
		@RequestParam UUID paymentId
	) {
		ResaleOrderCompleteResponse response = resaleOrderService.completePayment(resaleOrderId, paymentId);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 정산 최종 완료 처리",
		description = "실제 은행 지급 완료 후 관리자가 정산 상태를 변경 API"
	)
	@PostMapping("/orders/{resaleOrderId}/settled")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiSuccessResponse<Void>> completeSettlement(
		@PathVariable UUID resaleOrderId
	) {
		resaleOrderService.completeSettlement(resaleOrderId);
		return wrap(null);
	}

	@Operation(
		summary = "주문별 거래 ID 목록 조회",
		description = "특정 리셀 주문에 포함된 거래 ID 목록을 조회 API"
	)
	@GetMapping("/orders/{resaleOrderId}/transactions")
	public ResponseEntity<ApiSuccessResponse<List<UUID>>> getTransactionIds(
		@PathVariable UUID resaleOrderId
	) {
		return wrap(resaleOrderService.getTransactionIds(resaleOrderId));
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
		ResaleHoldResponse response = resaleHoldService.holdResale(buyerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "리셀 점유 해제",
		description = "리셀 점유 해제 API"
	)
	@PostMapping("/holds/{holdId}/release")
	public ResponseEntity<ApiSuccessResponse<ResaleReleaseResponse>> releaseResale(
		@AuthenticationPrincipal(expression = "id") UUID buyerId,
		@PathVariable UUID holdId
	) {
		ResaleReleaseResponse response = resaleHoldService.releaseResaleHold(buyerId, holdId);
		return wrap(response);
	}
}