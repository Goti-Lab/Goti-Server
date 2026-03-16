package com.goti.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.dto.request.PaymentRequest;
import com.goti.dto.response.PaymentResponse;
import com.goti.service.application.OrderPaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class PaymentController {
	private final OrderPaymentService orderPaymentService;

	@Operation(
		summary = "결제 요청",
		description = "특정 주문에 대한 mock 결제 요청 API"
	)
	@PostMapping("/{orderId}/payments")
	public ResponseEntity<ApiSuccessResponse<PaymentResponse>> create(
		@PathVariable UUID orderId,
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@Valid @RequestBody PaymentRequest request
	) {
		return wrap(
			orderPaymentService.create(
				orderId,
				memberId,
				request.paymentMethod(),
				request.idempotencyKey()
			)
		);
	}
}
