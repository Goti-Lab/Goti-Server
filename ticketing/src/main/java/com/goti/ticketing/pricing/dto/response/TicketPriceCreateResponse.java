package com.goti.ticketing.pricing.dto.response;

import java.util.UUID;

import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketType;
import com.goti.ticketing.domain.entity.pricing.TicketPriceEntity;

import io.swagger.v3.oas.annotations.media.Schema;

public record TicketPriceCreateResponse(
	@Schema(description = "티켓 가격 ID", example = "33333333-3333-3333-3333-333333333333")
	UUID priceId,

	@Schema(description = "좌석 등급 ID", example = "22222222-2222-2222-2222-222222222222")
	UUID gradeId,

	@Schema(description = "권종", example = "ADULT")
	TicketType ticketType,

	@Schema(description = "요일 유형", example = "WEEKDAY")
	TicketPricingDayType dayType,

	@Schema(description = "리그 유형", example = "REGULAR")
	LeagueType leagueType,

	@Schema(description = "가격", example = "15000")
	Integer price
) {
	public static TicketPriceCreateResponse from(TicketPriceEntity ticketPrice) {
		return new TicketPriceCreateResponse(
			ticketPrice.getId(),
			ticketPrice.getGrade().getId(),
			ticketPrice.getTicketType(),
			ticketPrice.getDayType(),
			ticketPrice.getLeagueType(),
			ticketPrice.getPrice()
		);
	}
}
