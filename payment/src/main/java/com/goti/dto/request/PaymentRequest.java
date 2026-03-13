package com.goti.dto.request;

import com.goti.constants.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "mock 결제 요청")
public record PaymentRequest(
	@Schema(description = "결제 수단", example = "CARD")
	@NotNull(message = "결제 수단은 필수입니다.")
	PaymentMethod paymentMethod,

	@Schema(description = "결제 멱등 키", example = "payment-idempotency-key")
	@NotBlank(message = "결제 멱등 키는 필수입니다.")
	String idempotencyKey
) {
}
