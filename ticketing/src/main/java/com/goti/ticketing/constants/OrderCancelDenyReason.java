package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCancelDenyReason {

	USED("티켓 사용 완료"),
	EXPIRED("취소 가능 기간 초과"),
	NOT_REFUNDABLE("리셀 티켓 환불 불가");

	private final String description;
}
