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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Internal Payment", description = "내부 결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/payments")
public class PaymentInternalController {
	private final PaymentLedgerProcessService paymentLedgerProcessService;

	@Operation(
		summary = "미정산 금액 조회 (내부용)",
		description = "특정 사용자의 미정산 된 금액을 조회"
	)
	@GetMapping("/resales/unsettled")
	public ResponseEntity<ApiSuccessResponse<UnsettledAmountResponse>> getUnsettledAmounts(
		@RequestParam UUID sellerId
	) {
		return wrap(paymentLedgerProcessService.getUnsettledAmounts(sellerId));
	}
}
