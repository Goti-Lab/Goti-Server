package com.goti.resale.infra.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResaleTicketPurchaseInfo(
	UUID ticketId,
	String gameTitle,
	LocalDateTime gameDate,
	String seatInfo
) {
}
