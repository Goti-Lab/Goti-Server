package com.goti.ticketing.ticket.service.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
	private static final DateTimeFormatter TICKET_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

	private final TicketRepository ticketRepository;

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
