package com.goti.payment.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "리셀 생성을 위한 리셀 거래 내용 요청")
public record ResaleTransactionItemRequest(
	@Schema(description = "거래 ID", example = "8df84c70-833e-4374-85ad-fa52f92f939e")
	@NotNull(message = "거래 ID는 필수입니다.")
	UUID transactionId,

	@Schema(description = "판매자 ID", example = "8df84c70-833e-4374-85ad-fa52f92f939e")
	@NotNull(message = "판매자 ID는 필수입니다.")
	UUID sellerId,

	@Schema(description = "판매자 정산 예정액", example = "46000")
	@Positive(message = "판매자 정산 예정액은 양수여야 합니다.")
	long settlementAmount
) {
}
