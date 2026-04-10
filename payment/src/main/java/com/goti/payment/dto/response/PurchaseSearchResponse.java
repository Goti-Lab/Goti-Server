package com.goti.payment.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record PurchaseSearchResponse(
	String purchaseType,
	UUID orderId,
	String orderNumber,
	String orderStatus,
	Integer totalQuantity,
	Integer totalAmount,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime orderedAt,
	UUID gameId,
	UUID stadiumId,
	String gameTitle,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,
	List<String> seatInfos,
	List<UUID> ticketIds
) {
}
