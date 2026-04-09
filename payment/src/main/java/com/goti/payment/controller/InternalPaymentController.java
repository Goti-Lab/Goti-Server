package com.goti.payment.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.payment.dto.response.UnsettledAmountResponse;
import com.goti.payment.service.application.PaymentLedgerProcessService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class InternalPaymentController {
	private final PaymentLedgerProcessService paymentLedgerProcessService;

	@GetMapping("/resales/unsettled")
	public ResponseEntity<ApiSuccessResponse<UnsettledAmountResponse>> getUnsettledAmounts(
		@RequestParam UUID userId
	) {
		return wrap(paymentLedgerProcessService.getUnsettledAmounts(userId));
	}
}
