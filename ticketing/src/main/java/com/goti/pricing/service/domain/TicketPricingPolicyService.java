package com.goti.pricing.service.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.constants.TicketPricingDayType;
import com.goti.constants.TicketPricingMatchType;
import com.goti.constants.TicketType;
import com.goti.pricing.dto.response.TicketPricingPolicyCreateResponse;

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
