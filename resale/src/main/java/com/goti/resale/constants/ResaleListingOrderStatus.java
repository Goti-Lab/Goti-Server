package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleListingOrderStatus {
	LISTING("판매중"),
	PARTIAL("부분 처리"),
	SOLD("판매 완료(정산 대기)"),
	SETTLED("판매 완료(정산 완료)"),
	CANCELED("판매 취소");

	private final String description;
}
