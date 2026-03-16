package com.goti.ticketing.seat.service.domain;

import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;

import java.time.LocalDateTime;

public interface SeatHoldExpiryService {
	void expire(
		SeatStatusEntity seatStatus,
		SeatHoldEntity seatHold,
		LocalDateTime now
	);
}
