package com.goti.ticketing.seat.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSeatSectionRequest(
	@Schema(description = "좌석 등급 ID", example = "11111111-1111-1111-1111-111111111111")
	@NotNull(message = "좌석 등급 ID는 필수입니다.")
	UUID gradeId,

	@Schema(description = "구장 ID", example = "22222222-2222-2222-2222-222222222222")
	@NotNull(message = "구장 ID는 필수입니다.")
	UUID stadiumId,

	@Schema(description = "구역 코드", example = "T3-1")
	@NotBlank(message = "구역 코드는 필수입니다.")
	String sectionCode,

	@Schema(description = "수용 인원", example = "120")
	@NotNull(message = "수용 인원은 필수입니다.")
	@Positive(message = "수용 인원은 0보다 커야 합니다")
	Integer capacity
) {
}
