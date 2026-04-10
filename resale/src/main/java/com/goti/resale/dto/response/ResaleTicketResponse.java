package com.goti.resale.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

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
}
