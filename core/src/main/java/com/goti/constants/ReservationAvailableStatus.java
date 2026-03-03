package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationAvailableStatus {

	ON_SALE("판매중"),
	PENDING("예매 예정"),
	SOLD_OUT("매진"),
	DISABLED("예매 불가");

	private final String description;
}