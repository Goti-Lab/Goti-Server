package com.goti.seat.service.domain;

import com.goti.domain.entity.seat.SeatHoldEntity;
import com.goti.domain.entity.seat.SeatStatusEntity;

import java.time.LocalDateTime;

public interface SeatHoldExpiryService {
	void expire(
		SeatStatusEntity seatStatus,
		SeatHoldEntity seatHold,
		LocalDateTime now
	);
}
