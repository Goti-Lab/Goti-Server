package com.goti.payment.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ResalePurchaseListItemResponse(
	UUID orderId,
	String orderNumber,
	String orderStatus,
	Integer totalQuantity,
	Integer totalAmount,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime orderedAt,
	UUID gameId,
	String gameTitle,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,
	List<String> seatInfos,
	List<UUID> ticketIds
) {
}
