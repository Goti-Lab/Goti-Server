package com.goti.ticketing.seat.service.application;

public record SeatHoldExpiryBatchResult(
	int attempted,
	int succeeded,
	int failed
) {
}
