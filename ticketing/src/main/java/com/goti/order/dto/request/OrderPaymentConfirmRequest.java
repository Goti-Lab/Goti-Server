package com.goti.order.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 결제 완료 처리 요청")
public record OrderPaymentConfirmRequest(
	@Schema(description = "주문자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	@NotNull(message = "주문자 ID는 필수입니다.")
	UUID userId,

	@Schema(description = "결제 ID", example = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
	UUID paymentId,

	@Schema(description = "PG 거래 ID", example = "mock-pg-tid")
	String pgTid
) {
}
