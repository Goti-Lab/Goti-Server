package com.goti.ticketing.ticket.service.domain;

import static java.util.stream.Collectors.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.ticket.dto.response.ResaleTicketResponse;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
	private static final DateTimeFormatter TICKET_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

	private final TicketRepository ticketRepository;
	private final OrderItemRepository orderItemRepository;

	@Override
	@Transactional
	public TicketEntity create(
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
			generateTicketNumber(),
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
	public Map<UUID, TicketEntity> getByOrderItemIds(List<UUID> orderItemIds) {
		return ticketRepository.findAllByOrderItemIdIn(orderItemIds).stream()
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

		return TicketResponse.from(ticket);
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

		return ResaleTicketResponse.from(
			ticket,
			orderItem.getSeat().getId(),
			orderItem.getSeat().getSeatSection().getId(),
			orderItem.getSeat().getSeatSection().getSeatGrade().getId()
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
		Integer transactionPrice
	) {
		oldTicket.invalidate();

		TicketEntity newTicket = TicketEntity.create(
			generateTicketNumber(),
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

	private String generateTicketNumber() {
		String ticketNumber = "TKT" +
			LocalDate.now().format(TICKET_NUMBER_FORMATTER) +
			getTsid(6);

		return ticketNumber;
	}

	private String getTsid(int length) {
		String tsid = TsidCreator.getTsid().toString();
		return tsid.substring(tsid.length() - length);
	}
}
