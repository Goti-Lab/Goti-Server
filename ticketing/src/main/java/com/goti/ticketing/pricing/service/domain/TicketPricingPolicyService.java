package com.goti.ticketing.pricing.service.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketPricingMatchType;
import com.goti.ticketing.constants.TicketType;
import com.goti.ticketing.pricing.dto.response.TicketPricingPolicyCreateResponse;

public interface TicketPricingPolicyService {
	TicketPricingPolicyCreateResponse create(
		UUID teamId,
		LocalDate policyStartAt,
		LocalDate policyEndAt,
		List<TicketPriceCreateParam> prices
	);

	record TicketPriceCreateParam(
		UUID gradeId,
		TicketType ticketType,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType,
		Integer price
	) {
	}
}
