package com.goti.ticketing.ticket.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;

public record ResaleTicketResponse(
	UUID ticketId,
	UUID ownerId,
	UUID gameId,
	UUID seatId,
	UUID sectionId,
	UUID gradeId,
	String seatInfo,
	Integer ticketPrice,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,
	UUID transactionId,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
	Instant createdAt
) {
	public static ResaleTicketResponse from(
		TicketEntity ticket,
		UUID seatId,
		UUID sectionId,
		UUID gradeId
	) {
		return new ResaleTicketResponse(
			ticket.getId(),
			ticket.getUserId(),
			ticket.getGameId(),
			seatId,
			sectionId,
			gradeId,
			ticket.getSeatInfo(),
			ticket.getTicketPrice(),
			ticket.getGameDate(),
			ticket.getResaleTransactionId(),
			ticket.getCreatedAt()
		);
	}
}
