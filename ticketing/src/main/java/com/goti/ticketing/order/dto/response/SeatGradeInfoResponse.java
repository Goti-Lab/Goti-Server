package com.goti.ticketing.order.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 등급별 좌석 정보 묶음")
public record SeatGradeInfoResponse(
	@Schema(description = "좌석 등급명", example = "1루 K8석")
	String seatGradeName,

	@Schema(description = "좌석 정보 목록", example = "[\"109구역 1열 8번\", \"109구역 1열 9번\"]")
	List<String> seatInfos
) {
}
