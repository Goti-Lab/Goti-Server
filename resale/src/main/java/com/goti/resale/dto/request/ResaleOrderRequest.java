package com.goti.resale.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record ResaleOrderRequest(
	@Schema(description = "리셀 점유 ID 목록", example = "[\"8df84c70-833e-4374-85ad-fa52f92f939e\",\"7a727b68-e223-4574-accf-7cba50f85fc8\"]")
	@NotEmpty(message = "리셀 점유 ID 목록은 필수입니다.")
	List<UUID> holdIds,

	@Schema(description = "구매자 닉네임", example = "홍길동")
	@NotEmpty(message = "구매자 닉네임은 필수입니다.")
	String buyerNickname,

	@Schema(description = "구매자 이메일", example = "buyer@goti.com")
	@NotEmpty(message = "구매자 이메일은 필수입니다.")
	String buyerEmail,

	@Schema(description = "구매자 전화번호", example = "01012345678")
	@NotEmpty(message = "구매자 전화번호는 필수입니다.")
	String buyerPhone
) {
}
