package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketingStatus {
	SCHEDULED("판매 예정"),
	AVAILABLE("구매 가능"),
	EXHAUSTED("매진"),
	TERMINATED("판매 종료"),
	CANCELED("판매 취소"),
	PAUSED("일시 중단")
	;

	private final String description;
}
