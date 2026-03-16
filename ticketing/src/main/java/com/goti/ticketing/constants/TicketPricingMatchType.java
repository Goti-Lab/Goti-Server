package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketPricingMatchType {

	EXHIBITION("시범 경기"),
	REGULAR("정규리그"),
	POST_SEASON("포스트시즌");

	private final String description;
}
