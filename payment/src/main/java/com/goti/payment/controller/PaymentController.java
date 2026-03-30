package com.goti.payment.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.payment.dto.request.PaymentCancelRequest;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.payment.dto.request.PaymentRequest;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.service.application.OrderPaymentService;
import com.goti.payment.service.domain.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
	private final OrderPaymentService orderPaymentService;
	private final PaymentService paymentService;

	@Operation(
		summary = "결제 요청",
		description = "특정 주문에 대한 mock 결제 요청 API"
	)
	@PostMapping("/orders/{orderId}")
	public ResponseEntity<ApiSuccessResponse<PaymentResponse>> create(
		@PathVariable UUID orderId,
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@Valid @RequestBody PaymentRequest request
	) {
		return wrap(
			orderPaymentService.initPayment(
				orderId,
				memberId,
				request.paymentMethod(),
				request.idempotencyKey()
			)
		);
	}

	@Operation(
		summary = "결제 정보 조회",
		description = "특정 주문에 대한 결제 정보 조회 API"
	)
	@GetMapping("/orders/{orderId}")
	public ResponseEntity<ApiSuccessResponse<PaymentResponse>> get(
		@PathVariable UUID orderId,
		@AuthenticationPrincipal(expression = "id") UUID memberId
	) {
		return wrap(orderPaymentService.getByOrderId(orderId, memberId));
	}


	@Operation(
		summary = "결제 취소 (내부용)",
		description = "주문 취소 시 mock 결제 취소 처리 API"
	)
	@PostMapping("/orders/{orderId}/cancellations")
	public ResponseEntity<ApiSuccessResponse<PaymentResponse>> cancel(
		@PathVariable UUID orderId,
		@Valid @RequestBody PaymentCancelRequest request
	) {
		return wrap(paymentService.cancel(orderId, request.cancellationId()));
	}
}
