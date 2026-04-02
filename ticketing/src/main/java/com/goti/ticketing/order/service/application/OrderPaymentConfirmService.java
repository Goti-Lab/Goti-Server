package com.goti.ticketing.order.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.OrderStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.game.service.application.GameTicketManagementService;
import com.goti.ticketing.order.dto.response.OrderPaymentConfirmResponse;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.order.repository.OrderRepository;
import com.goti.ticketing.seat.repository.SeatStatusRepository;
import com.goti.ticketing.seat.service.domain.SeatHoldService;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.service.application.TicketCreateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentConfirmService {
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final SeatStatusRepository seatStatusRepository;
	private final SeatHoldService seatHoldService;
	private final TicketCreateService ticketCreateService;
	private final GameTicketManagementService gameTicketManagementService;

	@Transactional
	public OrderPaymentConfirmResponse confirm(
		UUID orderId,
		UUID userId,
		UUID paymentId,
		String pgTid
	) {
		log.info(
			"action=PAYMENT_CONFIRM_START orderId={} userId={} paymentId={} pgTid={}",
			orderId,
			userId,
			paymentId,
			pgTid
		);

		OrderEntity order = orderRepository.findByIdAndMemberId(orderId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		Preconditions.validate(
			order.getOrderStatus() == OrderStatus.PENDING,
			ErrorCode.ORDER_PAYMENT_NOT_ALLOWED
		);

		order.confirm();

		List<OrderItemEntity> orderItems = orderItemRepository.findOrderItemsByOrderId(orderId);
		for (OrderItemEntity orderItem : orderItems) {
			SeatHoldEntity seatHold = getValidActiveHold(order, orderItem);

			SeatStatusEntity seatStatus = seatStatusRepository.findByGameAndSeat(
				order.getGameSchedule(), orderItem.getSeat()
				).orElseThrow(
					() -> new CustomException(ErrorCode.SEAT_STATUS_NOT_FOUND)
				);

			seatStatus.sell();
			seatHold.release();
			orderItem.pay();
		}

		List<TicketResponse> tickets = ticketCreateService.create(order);
		gameTicketManagementService.processSoldout(order.getGameSchedule());

		log.info(
			"action=PAYMENT_CONFIRM gameId={} userId={} orderId={} ticketCount={}",
			order.getGameSchedule().getId(),
			order.getMemberId(),
			order.getId(),
			tickets.size()
		);

		return OrderPaymentConfirmResponse.from(
			order.getId(),
			order.getOrderStatus(),
			tickets.size()
		);
	}

	private SeatHoldEntity getValidActiveHold(OrderEntity order, OrderItemEntity orderItem) {
		SeatHoldEntity seatHold = seatHoldService.findSeatHold(orderItem.getHoldId());

		Preconditions.validate(
			seatHold.getGameSchedule().getId().equals(order.getGameSchedule().getId()),
			ErrorCode.SEAT_HOLD_GAME_MISMATCH
		);
		Preconditions.validate(
			seatHold.getSeat().getId().equals(orderItem.getSeat().getId()),
			ErrorCode.SEAT_HOLD_NOT_FOUND
		);
		Preconditions.validate(
			seatHold.getUserId().equals(order.getMemberId()),
			ErrorCode.AUTH_PERMISSION_DENIED
		);
		Preconditions.validate(
			seatHold.getStatus() == SeatHoldStatus.HOLDING,
			ErrorCode.SEAT_HOLD_STATUS_INVALID
		);

		if (!seatHold.getExpiredAt().isAfter(LocalDateTime.now())) {
			throw new CustomException(ErrorCode.SEAT_HOLD_EXPIRED)
				.withContext("action", "SESSION_BLOCK")
				.withContext("stage", "PAYMENT_CONFIRM")
				.withContext("gameId", order.getGameSchedule().getId())
				.withContext("userId", order.getMemberId())
				.withContext("orderId", order.getId())
				.withContext("seatId", orderItem.getSeat().getId())
				.withContext("holdId", seatHold.getId());
		}

		return seatHold;
	}
}
