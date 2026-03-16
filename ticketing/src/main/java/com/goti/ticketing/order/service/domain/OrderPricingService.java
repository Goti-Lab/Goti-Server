package com.goti.ticketing.order.service.domain;

import java.util.List;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

public interface OrderPricingService {
	OrderPricingResult calculate(
		GameScheduleEntity gameSchedule,
		List<SeatHoldEntity> holds
	);
}
