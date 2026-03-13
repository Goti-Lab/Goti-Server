package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketingStatus {
	UPCOMING("판매 예정"),
	OPEN("판매 중"),
	SOLD_OUT("매진"),
	CLOSED("판매 마감"),
	CANCELED("판매 취소"),
	PAUSED("일시 중단")
	;

	private final String description;
}
