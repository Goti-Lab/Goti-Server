package com.goti.queue.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "대기열 최종 입장 요청")
public record QueueSeatEnterRequest(
	@Schema(description = "대기열 진입 시 발급받은 JWE 토큰", example = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0...")
	@NotBlank(message = "queueToken은 필수입니다.")
	String queueToken
) {
}
