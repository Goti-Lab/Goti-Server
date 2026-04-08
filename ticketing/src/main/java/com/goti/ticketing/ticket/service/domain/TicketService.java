package com.goti.ticketing.ticket.service.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.ticket.dto.response.ResaleTicketResponse;
import com.goti.ticketing.ticket.dto.response.TicketPurchaseInfoResponse;
import com.goti.ticketing.ticket.dto.response.TicketResponse;

public interface TicketService {
	TicketEntity create(
		String ticketNumber,
		OrderItemEntity orderItem,
		UUID gameId,
		UUID userId,
		String userNickname,
		String userEmail,
		String userPhone,
		String gameTitle,
		LocalDateTime gameDate,
		String seatInfo,
		Integer ticketPrice
	);

	Map<UUID, TicketEntity> getByOrderItemIds(List<UUID> orderItemIds);

	void invalidate(TicketEntity ticket);

	TicketResponse getDetail(
		UUID ticketId,
		UUID userId
	);

	List<TicketPurchaseInfoResponse> getPurchaseInfos(List<UUID> ticketIds);

	TicketEntity get(UUID ticketId);

	ResaleTicketResponse getResaleTicketInfo(UUID ticketId, UUID userId);

	int getOwnedTicketCountWithGame(UUID userId, UUID gameId);

	int getOwnedTicketCount(UUID userId);

	TicketEntity createByResale(
		TicketEntity oldTicket,
		UUID buyerId,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone,
		UUID transactionId,
		Integer transactionPrice,
		String ticketNumber
	);
}
