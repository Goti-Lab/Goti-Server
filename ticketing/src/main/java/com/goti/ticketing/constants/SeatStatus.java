package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatStatus {

	AVAILABLE("사용 가능"),
	HELD("임시 점유"),
	SOLD("판매 완료"),
	BLOCKED("사용 불가");

	private final String description;
}
