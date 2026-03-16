package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketResaleStatus {

	LISTED("판매중"),
	SOLD_PENDING("정산 대기"),
	SOLD_SETTLED("판매 완료"),
	UNLISTING("동결");

	private final String description;
}
