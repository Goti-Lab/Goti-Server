package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCancellationDenyReasonCode {

	TICKET_USED("티켓 사용 완료"),
	POLICY_WINDOW_EXPIRED("취소 가능 기간 초과"),
	RESALE_NOT_REFUNDABLE("리셀 티켓 환불 불가");

	private final String description;
}
