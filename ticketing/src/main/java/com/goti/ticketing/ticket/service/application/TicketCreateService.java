package com.goti.ticketing.ticket.service.application;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.order.OrderHistoryEntity;
import com.goti.exception.CustomException;
import com.goti.ticketing.infra.api.StadiumClient;
import com.goti.ticketing.infra.api.dto.response.BaseballTeamDisplayNameResponse;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.order.repository.OrderHistoryRepository;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.service.domain.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketCreateService {
	private static final String TICKET_NUMBER_PREFIX = "TKT-";
	private static final DateTimeFormatter TICKET_NUMBER_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMdd")
		.withZone(ZoneId.of("Asia/Seoul"));
	private static final int ORDER_SUFFIX_START_INDEX = 12;

	private final OrderHistoryRepository orderHistoryRepository;
	private final OrderItemRepository orderItemRepository;
	private final TicketService ticketService;
	private final StadiumClient stadiumClient;

	@Transactional
	public List<TicketResponse> create(OrderEntity order) {
		OrderHistoryEntity orderHistory = orderHistoryRepository.findByOrder_Id(order.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_HISTORY_NOT_FOUND));

		List<OrderItemEntity> orderItems = orderItemRepository.findOrderItemsByOrderId(order.getId());
		String ticketNumberPrefix = createPrefix(order.getCreatedAt(), order.getOrderNumber());
		String gameTitle = createGameTitle(order);

		return IntStream.range(0, orderItems.size())
			.mapToObj(index -> createTicket(
				order,
				orderHistory,
				orderItems.get(index),
				ticketNumberPrefix,
				gameTitle,
				index + 1
			))
			.toList();
	}

	private TicketResponse createTicket(
		OrderEntity order,
		OrderHistoryEntity orderHistory,
		OrderItemEntity orderItem,
		String ticketNumberPrefix,
		String gameTitle,
		int ticketSequence
	) {
		return TicketResponse.from(
			ticketService.create(
				generateTicketNumber(ticketNumberPrefix, ticketSequence),
				orderItem,
				order.getGameSchedule().getId(),
				order.getMemberId(),
				orderHistory.getName(),
				orderHistory.getEmail(),
				orderHistory.getMobile(),
				gameTitle,
				order.getGameSchedule().getStartAt(),
				buildSeatInfo(orderItem),
				orderItem.getTicketPrice()
			),
			orderItem.getSeat().getSeatSection().getSeatGrade().getName()
		);
	}

	public String createPrefix(Instant createdAt, String orderNumber) {
		return String.join("",
			TICKET_NUMBER_PREFIX,
			TICKET_NUMBER_DATE_FORMATTER.format(createdAt.atZone(ZoneId.of("Asia/Seoul"))),
			extractSuffix(orderNumber)
		);
	}

	private String extractSuffix(String orderNumber) {
		return Optional.ofNullable(orderNumber)
			.filter(s -> s.length() >= ORDER_SUFFIX_START_INDEX)
			.map(s -> s.substring(ORDER_SUFFIX_START_INDEX))
			.orElse("");
	}

	private String generateTicketNumber(String ticketNumberPrefix, int ticketSequence) {
		return ticketNumberPrefix + "-" + String.format("%03d", ticketSequence);
	}

	private String createGameTitle(OrderEntity order) {
		UUID homeTeamId = order.getGameSchedule().getHomeTeamId();
		UUID awayTeamId = order.getGameSchedule().getAwayTeamId();

		Map<UUID, String> teamDisplayNames = stadiumClient.getBaseballTeamDisplayNames(
				List.of(homeTeamId, awayTeamId)
			).stream()
			.collect(Collectors.toMap(
				BaseballTeamDisplayNameResponse::teamId,
				BaseballTeamDisplayNameResponse::teamDisplayName
			));

		String homeTeamDisplayName = teamDisplayNames.get(homeTeamId);
		String awayTeamDisplayName = teamDisplayNames.get(awayTeamId);

		if (homeTeamDisplayName == null || awayTeamDisplayName == null) {
			throw new CustomException(ErrorCode.GAME_NOT_FOUND);
		}

		return homeTeamDisplayName + "vs" + awayTeamDisplayName;
	}

	private String buildSeatInfo(OrderItemEntity orderItem) {
		return orderItem.getSeat().getSeatSection().getSectionCode() +
			" " +
			orderItem.getSeat().getRowName() +
			"-" +
			orderItem.getSeat().getSeatNum();
	}
}
