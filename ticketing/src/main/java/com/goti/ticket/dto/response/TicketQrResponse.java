package com.goti.ticket.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모바일 티켓 QR 응답")
public record TicketQrResponse(
	@Schema(description = "티켓 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID ticketId,

	@Schema(description = "QR 토큰", example = "QR01K4Z8P2XA7B9CD")
	String qrToken,

	@Schema(description = "QR 만료 시각", example = "2026-03-10T18:33:00")
	LocalDateTime expiresAt
) {
	public static TicketQrResponse from(
		UUID ticketId,
		String qrToken,
		LocalDateTime expiresAt
	) {
		return new TicketQrResponse(ticketId, qrToken, expiresAt);
	}
}
