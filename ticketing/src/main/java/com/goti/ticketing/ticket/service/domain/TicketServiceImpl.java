package com.goti.ticketing.ticket.service.domain;

import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.constants.TicketStatus;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.infra.api.StadiumClient;
import com.goti.ticketing.infra.api.dto.response.StadiumLocationResponse;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.ticket.dto.response.ResaleTicketResponse;
import com.goti.ticketing.ticket.dto.response.TicketPurchaseInfoResponse;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
	private final TicketRepository ticketRepository;
	private final OrderItemRepository orderItemRepository;
	private final StadiumClient stadiumClient;

	@Override
	@Transactional
	public TicketEntity create(
		String ticketNumber,
		OrderItemEntity orderItem,
		UUID gameId,
		UUID memberId,
		String userNickname,
		String userEmail,
		String userPhone,
		String gameTitle,
		LocalDateTime gameDate,
		String seatInfo,
		Integer ticketPrice
	) {
		TicketEntity ticket = TicketEntity.create(
			ticketNumber,
			orderItem.getId(),
			null,
			gameId,
			memberId,
			userNickname,
			userEmail,
			userPhone,
			gameTitle,
			gameDate,
			seatInfo,
			ticketPrice,
			null
		);

		return ticketRepository.save(ticket);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, TicketEntity> getByOrderItemIds(List<UUID> orderItemIds, UUID userId) {
		return ticketRepository.findAllByOrderItemIdIn(
				orderItemIds,
				userId
			)
			.stream()
			.collect(toMap(TicketEntity::getOrderItemId, ticket -> ticket));
	}

	@Override
	@Transactional
	public void invalidate(TicketEntity ticket) {
		ticket.invalidate();
	}

	@Override
	@Transactional(readOnly = true)
	public TicketResponse getDetail(
		UUID ticketId,
		UUID userId
	) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);

		TicketEntity ticket = ticketRepository.findByIdAndUserId(ticketId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

		String seatGradeName = orderItemRepository.findById(ticket.getOrderItemId())
			.map(orderItem -> orderItem.getSeat().getSeatSection().getSeatGrade().getName())
			.orElse(null);

		return TicketResponse.from(ticket, seatGradeName);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TicketPurchaseInfoResponse> getPurchaseInfos(List<UUID> ticketIds) {
		Preconditions.validate(
			ticketIds != null && !ticketIds.isEmpty(),
			ErrorCode.TICKET_IDS_REQUIRED
		);

		return ticketRepository.findAllByIdIn(ticketIds).stream()
			.map(TicketPurchaseInfoResponse::from)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public TicketEntity get(UUID ticketId) {
		return ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public ResaleTicketResponse getResaleTicketInfo(UUID ticketId, UUID userId) {
		TicketEntity ticket = ticketRepository.findByIdAndUserId(ticketId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

		OrderItemEntity orderItem = orderItemRepository.findById(ticket.getOrderItemId())
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_ITEM_NOT_FOUND));

		List<StadiumLocationResponse> stadiumLocations = stadiumClient.getStadiumLocations(
			List.of(orderItem.getSeat().getSeatSection().getStadiumId())
		);

		String location = stadiumLocations.isEmpty() ? null : stadiumLocations.getFirst().stadiumLocation();

		return ResaleTicketResponse.from(
			ticket,
			orderItem.getSeat().getSeatSection().getStadiumId(),
			location,
			orderItem.getSeat().getId(),
			orderItem.getSeat().getSeatSection().getId(),
			orderItem.getSeat().getSeatSection().getSeatGrade().getId(),
			orderItem.getSeat().getSeatSection().getSeatGrade().getName()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public int getOwnedTicketCountWithGame(UUID userId, UUID gameId) {
		return ticketRepository.countByUserIdAndGameId(userId, gameId);
	}

	@Override
	@Transactional(readOnly = true)
	public int getOwnedTicketCount(UUID userId) {
		return ticketRepository.countByUserIdAndTicketStatusIn(
			userId,
			List.of(TicketStatus.ISSUED, TicketStatus.RESALE_ISSUED)
		);
	}

	@Override
	@Transactional
	public TicketEntity createByResale(
		TicketEntity oldTicket,
		UUID buyerId,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone,
		UUID transactionId,
		Integer transactionPrice,
		String ticketNumber
	) {
		oldTicket.invalidate();

		TicketEntity newTicket = TicketEntity.create(
			ticketNumber,
			oldTicket.getOrderItemId(),
			transactionId,
			oldTicket.getGameId(),
			buyerId,
			buyerNickname,
			buyerEmail,
			buyerPhone,
			oldTicket.getGameTitle(),
			oldTicket.getGameDate(),
			oldTicket.getSeatInfo(),
			oldTicket.getTicketPrice(),
			transactionPrice
		);

		return ticketRepository.save(newTicket);
	}
}
