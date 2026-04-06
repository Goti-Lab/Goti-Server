package com.goti.ticketing.ticket.service.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.ticketing.constants.TicketFreezeReason;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.game.repository.gameschedule.GameScheduleRepository;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.seat.repository.SeatGradeRepository;
import com.goti.ticketing.ticket.dto.response.ResaleTicketGameInfoResponse;
import com.goti.ticketing.ticket.dto.response.ResaleTicketResponse;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.service.domain.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketResaleService {

	private final TicketService ticketService;
	private final TicketFreezeManagementService ticketFreezeManagementService;
	private final GameScheduleRepository gameScheduleRepository;
	private final SeatGradeRepository seatGradeRepository;
	private final OrderItemRepository orderItemRepository;

	@Transactional(readOnly = true)
	public ResaleTicketResponse getResaleTicketInfo(UUID ticketId, UUID userId) {
		return ticketService.getResaleTicketInfo(ticketId, userId);
	}

	@Transactional(readOnly = true)
	public int getOwnedTicketCount(UUID userId, UUID gameId) {
		return ticketService.getOwnedTicketCountWithGame(userId, gameId);
	}

	@Transactional(readOnly = true)
	public List<UUID> getExpiredGameIds(LocalDateTime threshold) {
		return gameScheduleRepository.findAllByStartAtBefore(threshold).stream()
			.map(GameScheduleEntity::getId)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ResaleTicketGameInfoResponse> getUpcomingGames() {
		List<GameScheduleEntity> upcomingGames = gameScheduleRepository.findAllByStartAtAfter(LocalDateTime.now());

		return upcomingGames.stream()
			.flatMap(game -> {
				List<SeatGradeEntity> grades = seatGradeRepository.findAllByStadiumId(game.getStadiumId());
				return grades.stream()
					.map(grade -> new ResaleTicketGameInfoResponse(game.getId(), grade.getId()));
			})
			.collect(Collectors.toList());
	}

	@Transactional
	public void markAsResaleListing(UUID ticketId, UUID userId) {
		TicketEntity ticket = ticketService.get(ticketId);
		validateOwnership(ticket, userId);
		ticket.markAsResaleListing();
	}

	@Transactional
	public void cancelResaleListing(UUID ticketId, UUID userId) {
		TicketEntity ticket = ticketService.get(ticketId);
		validateOwnership(ticket, userId);

		if (ticket.getUpdatedAt().isBefore(Instant.now().minus(1, ChronoUnit.HOURS))) {
			log.info("리셀 등록 1시간 경과 후 취소로 인한 티켓 동결: {}", ticketId);
			ticketFreezeManagementService.freezeTicket(ticketId, TicketFreezeReason.RESALE_CANCEL_AFTER_ONE_HOUR);
		}

		ticket.restoreFromResale();
	}

	@Transactional
	public TicketResponse transferOwnership(
		UUID ticketId,
		UUID buyerId,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone,
		UUID transactionId,
		Integer transactionPrice
	) {
		TicketEntity oldTicket = ticketService.get(ticketId);

		TicketEntity newTicket = ticketService.createByResale(
			oldTicket,
			buyerId,
			buyerNickname,
			buyerEmail,
			buyerPhone,
			transactionId,
			transactionPrice
		);

		String seatGradeName = orderItemRepository.findById(newTicket.getOrderItemId())
			.map(orderItem -> orderItem.getSeat().getSeatSection().getSeatGrade().getName())
			.orElse(null);

		return TicketResponse.from(newTicket, seatGradeName);
	}

	private void validateOwnership(TicketEntity ticket, UUID userId) {
		if (!ticket.getUserId().equals(userId)) {
			throw new CustomException(ErrorCode.AUTH_PERMISSION_DENIED);
		}
	}
}
