package com.goti.ticketing.order.service.domain;

import java.time.DayOfWeek;
import java.util.List;

import com.goti.ticketing.constants.LeagueType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.TicketPricingDayType;
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
	private static final int BOOKING_FEE_PER_ITEM = 1000;

	private final TicketPriceRepository ticketPriceRepository;

	@Override
	@Transactional(readOnly = true)
	public OrderPricingResult calculate(
		GameScheduleEntity gameSchedule,
		List<SeatHoldEntity> holds
	) {
		TicketPricingDayType dayType = resolveDayType(gameSchedule);
		LeagueType leagueType = resolveLeagueType(gameSchedule);

		List<OrderPricingResult.PricedHold> pricedHolds = holds.stream()
			.map(hold -> new OrderPricingResult.PricedHold(
				hold,
				getTicketPrice(hold, gameSchedule, dayType, leagueType).getPrice()
			))
			.toList();

		int ticketTotalAmount = pricedHolds.stream()
			.mapToInt(OrderPricingResult.PricedHold::ticketPrice)
			.sum();
		int totalAmount = ticketTotalAmount + (pricedHolds.size() * BOOKING_FEE_PER_ITEM);

		return new OrderPricingResult(totalAmount, pricedHolds);
	}

	private TicketPriceEntity getTicketPrice(
		SeatHoldEntity hold,
		GameScheduleEntity gameSchedule,
		TicketPricingDayType dayType,
		LeagueType leagueType
	) {
		return ticketPriceRepository.findApplicableTicketPrice(
				gameSchedule.getHomeTeamId(),
				gameSchedule.getStartAt().toLocalDate(),
				hold.getSeat().getSeatSection().getSeatGrade(),
				TicketType.ADULT,
				dayType,
				leagueType
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

	private LeagueType resolveLeagueType(GameScheduleEntity gameSchedule) {
		return switch (gameSchedule.getLeagueType()) {
			case EXHIBITION -> LeagueType.EXHIBITION;
			case REGULAR -> LeagueType.REGULAR;
			case POST_SEASON -> LeagueType.POST_SEASON;
		};
	}
}
