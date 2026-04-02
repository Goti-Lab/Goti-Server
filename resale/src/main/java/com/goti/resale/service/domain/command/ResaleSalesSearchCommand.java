package com.goti.resale.service.domain.command;

import java.time.LocalDate;
import java.util.UUID;

import com.goti.resale.constants.ResaleOrderSearchStatus;

public record ResaleSalesSearchCommand(
	UUID sellerId,
	Integer months,
	LocalDate startDate,
	LocalDate endDate,
	ResaleOrderSearchStatus status,
	Integer page,
	Integer size
) {
}
