package com.goti.resale.service.domain.command;

import java.time.LocalDate;
import java.util.UUID;

public record ResaleSalesSearchCommand(
	UUID sellerId,
	Integer months,
	LocalDate startDate,
	LocalDate endDate,
	String status,
	Integer page,
	Integer size
) {
}
