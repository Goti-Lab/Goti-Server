package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleSettlementStatus {
	PENDING("대기상태"),
	COMPLETED("완료"),
	FAILED("실패");

	private final String description;
}
