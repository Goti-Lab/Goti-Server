package com.goti.resale.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleSettlementStatus {
	PENDING("대기상태"),
	COMPLETED("완료"),
	FAILED("실패");

	private final String description;
}
