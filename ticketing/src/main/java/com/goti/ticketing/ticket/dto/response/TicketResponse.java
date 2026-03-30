package com.goti.ticketing.ticket.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.ticketing.constants.ResaleEnabledStatus;
import com.goti.ticketing.constants.TicketStatus;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.domain.entity.ticket.TicketFreezeEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "티켓 상세 조회 응답")
public record TicketResponse(
	@Schema(description = "티켓 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID ticketId,

	@Schema(description = "티켓 번호", example = "TKT260313A1B2C3")
	String ticketNumber,

	@Schema(description = "주문 상세 ID", example = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
	UUID orderItemId,

	@Schema(description = "리셀 거래 ID", example = "cccccccc-cccc-cccc-cccc-cccccccccccc")
	UUID resaleTransactionId,

	@Schema(description = "경기 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID gameId,

	@Schema(description = "경기 제목", example = "두산 베어스 vs LG 트윈스")
	String gameTitle,

	@Schema(description = "경기 일시", example = "2026-03-15 18:30")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,

	@Schema(description = "좌석 정보", example = "VIP A구역 3열 15번")
	String seatInfo,

	@Schema(description = "티켓 가격", example = "24000")
	Integer ticketPrice,

	@Schema(description = "리셀 가격", example = "26000")
	Integer resalePrice,

	@Schema(description = "티켓 상태", example = "ISSUED")
	TicketStatus ticketStatus,

	@Schema(description = "리셀 가능 여부", example = "DISABLED")
	ResaleEnabledStatus resaleEnabledStatus,

	@Schema(description = "티켓 동결 여부", example = "false")
	boolean frozen,

	@Schema(description = "티켓 동결 종료 시각", example = "2026-03-15 22:30")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime frozenUntil,

	@Schema(description = "티켓 발급 일시", example = "2026-03-13 10:15")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
	Instant issuedAt,

	@Schema(description = "티켓 사용 일시", example = "2026-03-15 18:55")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime usedAt
) {
	public static TicketResponse from(TicketEntity ticket) {
		TicketFreezeEntity activeFreeze = ticket.getFreeze();
		boolean frozen = activeFreeze != null && activeFreeze.isActive();

		return new TicketResponse(
			ticket.getId(),
			ticket.getTicketNumber(),
			ticket.getOrderItemId(),
			ticket.getResaleTransactionId(),
			ticket.getGameId(),
			ticket.getGameTitle(),
			ticket.getGameDate(),
			ticket.getSeatInfo(),
			ticket.getTicketPrice(),
			ticket.getResalePrice(),
			ticket.getTicketStatus(),
			ticket.getResaleEnabledStatus(),
			frozen,
			frozen ? activeFreeze.getFrozenUntil() : null,
			ticket.getCreatedAt(),
			ticket.getUsedAt()
		);
	}
}
