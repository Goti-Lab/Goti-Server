package com.goti.ticketing.pricing.service.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.pricing.dto.response.TicketPricingPolicyCreateResponse;
import com.goti.ticketing.pricing.dto.response.TicketPricingPolicyResponse;
import com.goti.ticketing.pricing.service.domain.command.TicketPriceCreateCommand;

public interface TicketPricingPolicyService {
	TicketPricingPolicyCreateResponse create(
		UUID teamId,
		LocalDate policyStartAt,
		LocalDate policyEndAt,
		List<TicketPriceCreateCommand> prices
	);

	TicketPricingPolicyResponse get(UUID teamId, UUID memberId);
}
