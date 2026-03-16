package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketPricingDayType {

	WEEKDAY("평일"),
	WEEKEND("주말");

	private final String description;
}
