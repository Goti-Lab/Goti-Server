package com.goti.ticketing.order.service.domain;

import java.util.List;

import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

public record OrderPricingResult(
	Integer totalAmount,
	List<PricedHold> pricedHolds
) {
	public record PricedHold(
		SeatHoldEntity hold,
		Integer ticketPrice
	) {
	}
}
