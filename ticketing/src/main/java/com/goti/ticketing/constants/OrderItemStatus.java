package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderItemStatus {

	RESERVED("예약"),
	PAID("결제 완료"),
	CANCELED("취소"),
	CANCEL_FAILED("취소 실패");

	private final String description;
}
