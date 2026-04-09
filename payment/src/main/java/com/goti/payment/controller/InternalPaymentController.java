package com.goti.payment.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.payment.dto.request.PaymentCancelRequest;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.dto.response.UnsettledAmountResponse;
import com.goti.payment.service.application.PaymentLedgerProcessService;
import com.goti.payment.service.domain.PaymentService;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class InternalPaymentController {
	private final PaymentLedgerProcessService paymentLedgerProcessService;
	private final PaymentService paymentService;

	@GetMapping("/resales/unsettled")
	public ResponseEntity<ApiSuccessResponse<UnsettledAmountResponse>> getUnsettledAmounts(
		@RequestParam UUID userId
	) {
		return wrap(paymentLedgerProcessService.getUnsettledAmounts(userId));
	}

	@PostMapping("/orders/{orderId}/cancellations")
	public ResponseEntity<ApiSuccessResponse<PaymentResponse>> cancel(
		@PathVariable UUID orderId,
		@Valid @RequestBody PaymentCancelRequest request
	) {
		return wrap(paymentService.cancel(orderId, request.cancellationId()));
	}
}
