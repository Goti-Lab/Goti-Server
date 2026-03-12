package com.goti.order.service.domain;

import java.util.List;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.seat.SeatHoldEntity;

public interface OrderPricingService {
	OrderPricingResult calculate(
		GameScheduleEntity gameSchedule,
		List<SeatHoldEntity> holds
	);
}
