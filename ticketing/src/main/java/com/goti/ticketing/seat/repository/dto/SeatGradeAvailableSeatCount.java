package com.goti.ticketing.seat.repository.dto;

import java.util.UUID;

public record SeatGradeAvailableSeatCount(
	UUID seatGradeId,
	Long availableSeatCount
) {
}
