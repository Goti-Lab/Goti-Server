package com.goti.ticketing.pricing.service.domain.command;

import java.util.UUID;

import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketType;

public record TicketPriceCreateCommand(
	UUID gradeId,
	TicketType ticketType,
	TicketPricingDayType dayType,
	LeagueType leagueType,
	Integer price
) {
}
