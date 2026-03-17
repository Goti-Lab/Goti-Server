package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleOrderStatus {
	PENDING("주문대기"),
	COMPLETED("주문완료");

	private final String description;
}
