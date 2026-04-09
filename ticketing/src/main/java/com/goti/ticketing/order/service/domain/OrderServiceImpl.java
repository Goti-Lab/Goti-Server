package com.goti.ticketing.order.service.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.infra.api.StadiumClient;
import com.goti.ticketing.infra.api.dto.response.StadiumLocationResponse;
import com.goti.ticketing.order.dto.response.OrderListResponse;
import com.goti.ticketing.order.dto.response.OrderPaymentInfoResponse;
import com.goti.ticketing.order.dto.response.SeatGradeInfoResponse;
import com.goti.ticketing.order.repository.OrderRepository;
import com.goti.ticketing.session.service.application.ReservationSessionService;
import com.goti.ticketing.ticket.service.domain.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final List<Integer> ALLOWED_MONTHS = List.of(1, 3, 6);

	private final OrderRepository orderRepository;
	private final OrderItemService orderItemService;
	private final TicketService ticketService;
	private final StadiumClient stadiumClient;
	private final ReservationSessionService reservationSessionService;

	@Override
	@Transactional
	public OrderEntity create(
		UUID memberId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	) {
		OrderEntity order = OrderEntity.create(
			generateOrderNumber(),
			memberId,
			gameSchedule,
			totalQuantity,
			totalAmount
		);

		return orderRepository.save(order);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderEntity get(UUID orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
	}

	@Override
	public void expire(OrderEntity order) {
		order.expire();
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderListResponse> getOrders(
		UUID memberId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);
		validatePeriodFilter(months, startDate, endDate);

		List<OrderEntity> orders = orderRepository.findOrders(memberId, months, startDate, endDate);

		List<UUID> stadiumIds = orders.stream()
			.map(order -> order.getGameSchedule().getStadiumId())
			.distinct()
			.toList();

		Map<UUID, String> stadiumLocations = getStadiumLocationsMap(stadiumIds);

		return orders.stream()
			.map(order -> toOrderListResponse(
				order,
				stadiumLocations.get(order.getGameSchedule().getStadiumId())
			))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public OrderPaymentInfoResponse getPaymentOrder(UUID orderId, UUID memberId) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);
		OrderEntity order = orderRepository.findByIdAndMemberId(orderId, memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		reservationSessionService.validateActiveSession(memberId, order.getGameSchedule().getId());
		return OrderPaymentInfoResponse.from(order);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderEntity get(UUID orderId, UUID memberId) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);

		return orderRepository.findByIdAndMemberId(orderId, memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
	}

	@Override
	@Transactional
	public void cancel(OrderEntity order) {
		order.cancel();
	}

	@Override
	@Transactional
	public void partialCancel(OrderEntity order) {
		order.partialCancel();
	}

	private String generateOrderNumber() {
		String tsidSuffix = TsidCreator.getTsid().toString();
		return "ORD" + "-" +
			LocalDate.now().format(ORDER_NUMBER_FORMATTER) +
			tsidSuffix.substring(tsidSuffix.length() - 6);
	}

	private void validatePeriodFilter(
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Preconditions.validate(
			months == null || (startDate == null && endDate == null),
			ErrorCode.ORDER_HISTORY_PERIOD_FILTER_CONFLICT
		);

		Preconditions.validate(
			(startDate == null) == (endDate == null),
			ErrorCode.ORDER_HISTORY_PERIOD_DATE_REQUIRED
		);

		if (months != null) {
			Preconditions.validate(
				ALLOWED_MONTHS.contains(months),
				ErrorCode.ORDER_HISTORY_PERIOD_MONTHS_INVALID
			);
		}

		if (startDate != null && endDate != null) {
			Preconditions.validate(
				!startDate.isAfter(endDate),
				ErrorCode.ORDER_HISTORY_PERIOD_INVALID_RANGE
			);
		}
	}

	private OrderListResponse toOrderListResponse(
		OrderEntity order,
		String stadiumLocation
	) {
		List<OrderItemEntity> orderItems = orderItemService.get(order.getId());
		Map<UUID, OrderItemEntity> orderItemsById = orderItems.stream()
			.collect(Collectors.toMap(OrderItemEntity::getId, orderItem -> orderItem));
		List<UUID> orderItemIds = orderItems.stream()
			.map(OrderItemEntity::getId)
			.toList();

		Map<UUID, TicketEntity> ticketsByOrderItemId = ticketService.getByOrderItemIds(orderItemIds, order.getMemberId());
		List<TicketEntity> tickets = orderItemIds.stream()
			.map(ticketsByOrderItemId::get)
			.toList();

		TicketEntity representativeTicket = tickets.getFirst();
		List<SeatGradeInfoResponse> seatGradeGroups = tickets.stream()
			.collect(Collectors.groupingBy(
				ticket -> orderItemsById.get(ticket.getOrderItemId()).getSeat().getSeatSection().getSeatGrade().getName(),
				LinkedHashMap::new,
				Collectors.mapping(TicketEntity::getSeatInfo, Collectors.toList())
			))
			.entrySet().stream()
			.map(entry -> new SeatGradeInfoResponse(entry.getKey(), entry.getValue()))
			.toList();

		return OrderListResponse.of(
			order,
			representativeTicket.getGameTitle(),
			representativeTicket.getGameDate(),
			stadiumLocation,
			seatGradeGroups
		);
	}

	private Map<UUID, String> getStadiumLocationsMap(List<UUID> stadiumIds) {
		return stadiumClient
			.getStadiumLocations(stadiumIds).stream()
			.collect(Collectors.toMap(
				StadiumLocationResponse::stadiumId,
				StadiumLocationResponse::stadiumLocation
			));
	}

}
