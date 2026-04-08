package com.goti.resale.infra.dto;

import java.util.UUID;

public record TicketTransferRequest(
	UUID buyerId,
	String buyerNickname,
	String buyerEmail,
	String buyerPhone,
	UUID transactionId,
	Integer transactionPrice,
	String ticketNumber
) {
}