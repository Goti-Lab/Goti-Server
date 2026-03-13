package com.goti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentStatus;
import com.goti.constants.PaymentType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "mock 결제 응답")
public record PaymentResponse(
	@Schema(description = "결제 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID paymentId,

	@Schema(description = "주문 ID", example = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
	UUID orderId,

	@Schema(description = "결제 처리 유형", example = "PAYMENT")
	PaymentType paymentType,

	@Schema(description = "결제 수단", example = "CARD")
	PaymentMethod paymentMethod,

	@Schema(description = "결제 금액", example = "24000")
	Integer paymentAmount,

	@Schema(description = "PG사", example = "MOCK")
	String pgProvider,

	@Schema(description = "PG 거래 ID", example = "mock-pg-tid")
	String pgTid,

	@Schema(description = "결제 상태", example = "SUCCESS")
	PaymentStatus paymentStatus,

	@Schema(description = "결제 완료 시각")
	LocalDateTime paidAt,

	@Schema(description = "결제 실패 사유", example = "mock 결제 실패")
	String failedReason
) {
	public static PaymentResponse from(
		UUID paymentId,
		UUID orderId,
		PaymentType paymentType,
		PaymentMethod paymentMethod,
		Integer paymentAmount,
		String pgProvider,
		String pgTid,
		PaymentStatus paymentStatus,
		LocalDateTime paidAt,
		String failedReason
	) {
		return new PaymentResponse(
			paymentId,
			orderId,
			paymentType,
			paymentMethod,
			paymentAmount,
			pgProvider,
			pgTid,
			paymentStatus,
			paidAt,
			failedReason
		);
	}
}
