package com.goti.pricing.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.goti.constants.TicketPricingDayType;
import com.goti.constants.TicketPricingMatchType;
import com.goti.constants.TicketType;
import com.goti.domain.entity.pricing.TicketPriceEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;

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
