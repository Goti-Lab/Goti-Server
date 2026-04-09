package com.goti.resale.infra.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ResaleTicketPurchaseInfo(
	UUID ticketId,
	String gameTitle,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,
	String seatInfo
) {
}
