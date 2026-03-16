package com.goti.ticketing.pricing.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketPricingMatchType;
import com.goti.ticketing.constants.TicketType;
import com.goti.ticketing.domain.entity.pricing.TicketPriceEntity;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;

public interface TicketPriceRepositoryCustom {
	Optional<TicketPriceEntity> findApplicableTicketPrice(
		UUID teamId,
		LocalDate playDate,
		SeatGradeEntity grade,
		TicketType ticketType,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType
	);
}
