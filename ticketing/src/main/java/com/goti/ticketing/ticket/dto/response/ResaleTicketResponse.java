package com.goti.ticketing.ticket.dto.response;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
	String gradeName,
	String seatInfo,
	Integer ticketPrice,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,
	String gameTitle,
	UUID stadiumId,
	String stadiumLocation,
	UUID transactionId,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime createdAt
) {
	public static ResaleTicketResponse from(
		TicketEntity ticket,
		UUID stadiumId,
		String stadiumLocation,
		UUID seatId,
		UUID sectionId,
		UUID gradeId,
		String gradeName
	) {
		return new ResaleTicketResponse(
			ticket.getId(),
			ticket.getUserId(),
			ticket.getGameId(),
			seatId,
			sectionId,
			gradeId,
			gradeName,
			ticket.getSeatInfo(),
			ticket.getTicketPrice(),
			ticket.getGameDate(),
			ticket.getGameTitle(),
			stadiumId,
			stadiumLocation,
			ticket.getResaleTransactionId(),
			LocalDateTime.ofInstant(ticket.getCreatedAt(), ZoneId.of("Asia/Seoul"))
		);
	}
}
