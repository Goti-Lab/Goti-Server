package com.goti.payment.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.payment.dto.request.ResalePaymentRequest;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.dto.response.ResalePaymentLedgerResponse;
import com.goti.payment.dto.response.UnsettledAmountResponse;
import com.goti.payment.service.application.PaymentLedgerProcessService;
import com.goti.payment.service.application.ResaleOrderPaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/resales")
public class PaymentResaleController {
	private final ResaleOrderPaymentService resaleOrderPaymentService;
	private final PaymentLedgerProcessService paymentLedgerProcessService;

	@Operation(
		summary = "리셀 결제 요청",
		description = "리셀 주문에 대한 결제 생성 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<PaymentResponse>> createResalePayment(
		@Valid @RequestBody ResalePaymentRequest request
	) {
		return wrap(
			resaleOrderPaymentService.createResaleEscrow(request)
		);
	}

	@Operation(
		summary = "리셀 에스크로 해제 요청",
		description = "티켓 소유권 이전 완료 후 에스크로를 해제하여 판매자에게 정산 지시 API"
	)
	@PatchMapping("/orders/{orderId}/release")
	public ResponseEntity<ApiSuccessResponse<Void>> releaseEscrow(
		@PathVariable UUID orderId
	) {
		resaleOrderPaymentService.releaseEscrow(orderId);
		return wrap(null);
	}

	@Operation(
		summary = "미정산 금액 조회",
		description = "미정산 된 금액을 조회"
	)
	@GetMapping("/unsettled")
	public ResponseEntity<ApiSuccessResponse<UnsettledAmountResponse>> getUnsettledAmounts(
		@AuthenticationPrincipal(expression = "id") UUID sellerId) {
		return wrap(paymentLedgerProcessService.getUnsettledAmounts(sellerId));
	}

	@Operation(
		summary = "장부 목록 조회",
		description = "모든 결제 장부 내역을 페이징하여 조회"
	)
	@GetMapping("/ledgers")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiSuccessResponse<Page<ResalePaymentLedgerResponse>>> getLedgers(
		Pageable pageable
	) {
		return wrap(paymentLedgerProcessService.getLedgers(pageable));
	}

	@Operation(
		summary = "주문별 장부 조회",
		description = "특정 주문 ID에 해당하는 장부를 조회"
	)
	@GetMapping("/ledgers/orders/{orderId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiSuccessResponse<ResalePaymentLedgerResponse>> getLedgerByOrderId(
		@PathVariable UUID orderId
	) {
		return wrap(paymentLedgerProcessService.getLedgerByOrderId(orderId));
	}
}
