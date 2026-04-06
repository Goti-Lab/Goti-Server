package com.goti.ticketing.ticket.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "티켓 구매 내역용 상세 응답")
public record TicketPurchaseInfoResponse(
	@Schema(description = "티켓 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID ticketId,
	@Schema(description = "경기 제목", example = "두산 베어스 vs LG 트윈스")
	String gameTitle,
	@Schema(description = "경기 일시", example = "2026-03-15 18:30")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,
	@Schema(description = "좌석 정보", example = "109구역 1열 8번")
	String seatInfo
) {
	public static TicketPurchaseInfoResponse from(TicketEntity ticket) {
		return new TicketPurchaseInfoResponse(
			ticket.getId(),
			ticket.getGameTitle(),
			ticket.getGameDate(),
			ticket.getSeatInfo()
		);
	}
}
