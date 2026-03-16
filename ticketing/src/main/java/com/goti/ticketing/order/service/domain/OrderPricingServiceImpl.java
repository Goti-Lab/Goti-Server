package com.goti.ticketing.order.service.domain;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketPricingMatchType;
import com.goti.ticketing.constants.TicketType;
import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.pricing.TicketPriceEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.exception.CustomException;
import com.goti.ticketing.pricing.repository.TicketPriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPricingServiceImpl implements OrderPricingService {
	private final TicketPriceRepository ticketPriceRepository;

	@Override
	@Transactional(readOnly = true)
	public OrderPricingResult calculate(
		GameScheduleEntity gameSchedule,
		List<SeatHoldEntity> holds
	) {
		TicketPricingDayType dayType = resolveDayType(gameSchedule);
		TicketPricingMatchType matchType = resolveMatchType(gameSchedule);

		List<OrderPricingResult.PricedHold> pricedHolds = holds.stream()
			.map(hold -> new OrderPricingResult.PricedHold(
				hold,
				getTicketPrice(hold, gameSchedule, dayType, matchType).getPrice()
			))
			.toList();

		int totalAmount = pricedHolds.stream()
			.mapToInt(OrderPricingResult.PricedHold::ticketPrice)
			.sum();

		return new OrderPricingResult(totalAmount, pricedHolds);
	}

	private TicketPriceEntity getTicketPrice(
		SeatHoldEntity hold,
		GameScheduleEntity gameSchedule,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType
	) {
		return ticketPriceRepository.findApplicableTicketPrice(
				gameSchedule.getHomeTeamId(),
				gameSchedule.getStartAt().toLocalDate(),
				hold.getSeat().getSeatSection().getSeatGrade(),
				TicketType.ADULT,
				dayType,
				matchType
			)
			.orElseThrow(() -> new CustomException(ErrorCode.TICKET_PRICE_NOT_FOUND));
	}

	private TicketPricingDayType resolveDayType(GameScheduleEntity gameSchedule) {
		DayOfWeek dayOfWeek = gameSchedule.getStartAt().getDayOfWeek();
		return switch (dayOfWeek) {
			case FRIDAY, SATURDAY, SUNDAY -> TicketPricingDayType.WEEKEND;
			default -> TicketPricingDayType.WEEKDAY;
		};
	}

	private TicketPricingMatchType resolveMatchType(GameScheduleEntity gameSchedule) {
		return switch (gameSchedule.getLeagueType()) {
			case PRE_SEASON -> TicketPricingMatchType.EXHIBITION;
			case POSTSEASON -> TicketPricingMatchType.POST_SEASON;
			case REGULAR -> TicketPricingMatchType.REGULAR;
		};
	}
}
