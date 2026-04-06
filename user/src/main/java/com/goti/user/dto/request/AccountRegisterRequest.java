package com.goti.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "계좌 등록 요청")
public record AccountRegisterRequest(

	@Schema(description = "계좌 번호", example = "1002-876-543219")
	@NotBlank(message = "계좌 번호는 필수 항목입니다.")
	String accountNumber,

	@Schema(description = "계좌 은행명", example = "우리은행")
	@NotBlank(message = "계좌 은행명은 필수 항목입니다.")
	String bankName,

	@Schema(description = "예금주", example = "홍길동")
	@NotBlank(message = "계좌 예금주는 필수 항목입니다.")
	String accountHolder
) {
}
