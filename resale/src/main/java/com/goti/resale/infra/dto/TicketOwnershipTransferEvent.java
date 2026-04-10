package com.goti.resale.infra.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record TicketOwnershipTransferEvent(
	UUID resaleOrderId,
	UUID buyerId,
	UUID paymentId,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime createdAt,
	String authToken
) {
	public static TicketOwnershipTransferEvent from(
		ResaleOrderPaymentCompletedEvent event,
		String authToken
	) {
		return new TicketOwnershipTransferEvent(
			event.resaleOrderId(),
			event.buyerId(),
			event.paymentId(),
			LocalDateTime.now(),
			authToken
		);
	}
}