package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

	PENDING("주문 대기"),
	CONFIRMED("주문 확정"),
	CANCELED("주문 취소"),
	PARTIALLY_CANCELED("부분 취소");

	private final String description;
}
