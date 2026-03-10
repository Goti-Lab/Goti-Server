package com.goti.order.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "점유 좌석 기반 주문 생성 요청")
public record OrderCreateRequest(
	@Schema(description = "경기 ID", example = "11111111-1111-1111-1111-111111111111")
	@NotNull(message = "경기 ID는 필수입니다.")
	UUID gameId,

	@Schema(description = "주문 생성 대상 hold ID 목록")
	@NotEmpty(message = "hold ID 목록은 필수입니다.")
	List<@NotNull(message = "hold ID는 필수입니다.") UUID> holdIds,

	@Schema(description = "구매자 이름", example = "홍길동")
	@NotBlank(message = "구매자 이름은 필수입니다.")
	String ordererName,

	@Schema(description = "구매자 연락처", example = "01012345678")
	@NotBlank(message = "구매자 연락처는 필수입니다.")
	String ordererPhone,

	@Schema(description = "구매자 이메일", example = "orderer@goti.com")
	@NotBlank(message = "구매자 이메일은 필수입니다.")
	String ordererEmail
) {
}
