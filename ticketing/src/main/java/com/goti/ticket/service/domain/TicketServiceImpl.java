package com.goti.ticket.service.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.domain.entity.order.OrderItemEntity;
import com.goti.domain.entity.ticket.TicketEntity;
import com.goti.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

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
