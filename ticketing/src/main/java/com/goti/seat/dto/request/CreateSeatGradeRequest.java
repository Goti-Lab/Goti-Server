package com.goti.seat.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateSeatGradeRequest(
	@Schema(description = "구장 ID", example = "11111111-1111-1111-1111-111111111111")
	@NotNull(message = "구장 ID는 필수입니다.")
	UUID stadiumId,

	@Schema(description = "좌석 등급명", example = "VIP")
	@NotBlank(message = "좌석 등급명은 필수입니다.")
	String name,

	@Schema(description = "좌석 등급 표시 색상 HEX", example = "#FFAA00")
	@Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "유효하지 않은 HEX 색상 코드 형식입니다.")
	String displayColorHex
) {
}
