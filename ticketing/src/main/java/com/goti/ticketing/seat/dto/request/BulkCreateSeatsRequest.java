package com.goti.ticketing.seat.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BulkCreateSeatsRequest(
	@NotNull(message = "구역 ID는 필수입니다.")
	UUID sectionId,

	@NotBlank(message = "행 이름은 필수입니다.")
	String rowName,

	@NotNull(message = "시작 좌석 번호는 필수입니다.")
	@Positive(message = "시작 좌석 번호는 0보다 커야합니다.")
	Integer startSeatNumber,

	@NotNull(message = "종료 좌석 번호는 필수입니다.")
	@Positive(message = "종료 좌석 번호는 0보다 커야합니다.")
	Integer endSeatNumber
) {
}
