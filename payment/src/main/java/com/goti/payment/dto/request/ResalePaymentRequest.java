package com.goti.payment.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "리셀 결제 요청")
public record ResalePaymentRequest(
	@Schema(description = "주문 ID", example = "8df84c70-833e-4374-85ad-fa52f92f939e")
	@NotNull(message = "주문 ID는 필수입니다.")
	UUID orderId,

	@Schema(description = "구매자 ID", example = "8df84c70-833e-4374-85ad-fa52f92f939e")
	@NotNull(message = "구매자 ID는 필수입니다.")
	UUID buyerId,

	@Schema(description = "구매자 총 결제 금액", example = "54000")
	@Positive(message = "구매자 총 금액은 양수여야 합니다.")
	int totalAmount,

	@Schema(description = "총 구매자 수수료 합계", example = "2000")
	@PositiveOrZero(message = "총 구매자 수수료는 0원 이상이어야 합니다.")
	int totalBuyerFee,

	@Schema(description = "총 판매자 수수료 합계", example = "2000")
	@PositiveOrZero(message = "총 판매자 수수료는 0원 이상이어야 합니다.")
	int totalSellerFee,

	@Schema(description = "리셀 거래 항목 리스트")
	@NotNull(message = "거래 항목은 필수입니다.")
	List<ResaleTransactionItemRequest> items,

	@Schema(description = "결제 수단", example = "CARD")
	@NotNull(message = "결제 수단은 필수입니다.")
	String paymentMethod,

	@Schema(description = "결제 멱등 키", example = "payment-idempotency-key")
	@NotNull(message = "결제 멱등 키는 필수입니다.")
	String idempotencyKey
) {
}
