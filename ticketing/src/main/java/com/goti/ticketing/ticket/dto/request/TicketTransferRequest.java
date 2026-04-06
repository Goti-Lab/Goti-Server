package com.goti.ticketing.ticket.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "티켓 주인 변경")
public record TicketTransferRequest(
	@NotNull(message = "구매자 ID는 필수입니다.")
	UUID buyerId,

	@NotBlank(message = "구매자 닉네임은 필수입니다.")
	String buyerNickname,

	@NotBlank(message = "구매자 이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String buyerEmail,

	@NotBlank(message = "구매자 전화번호는 필수입니다.")
	String buyerPhone,

	@NotNull(message = "리셀 거래 ID는 필수입니다.")
	UUID transactionId,

	@NotNull(message = "리셀 거래 가격은 필수입니다.")
	Integer transactionPrice
) {
}
