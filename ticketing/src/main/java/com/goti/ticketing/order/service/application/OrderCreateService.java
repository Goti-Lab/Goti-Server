package com.goti.ticketing.order.service.application;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.OrderItemStatus;
import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.constants.TicketType;
import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.exception.CustomException;
import com.goti.ticketing.game.repository.gameschedule.GameScheduleRepository;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.order.dto.response.OrderCreateResponse;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.order.service.domain.command.OrderCreateCommand;
import com.goti.ticketing.order.service.domain.OrderHistoryService;
import com.goti.ticketing.order.service.domain.OrderItemService;
import com.goti.ticketing.order.service.domain.OrderPricingResult;
import com.goti.ticketing.order.service.domain.OrderPricingService;
import com.goti.ticketing.order.service.domain.OrderService;
import com.goti.ticketing.seat.repository.SeatHoldRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCreateService {
	private final GameScheduleRepository gameScheduleRepository;
	private final SeatHoldRepository seatHoldRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderService orderService;
	private final OrderHistoryService orderHistoryService;
	private final OrderItemService orderItemService;
	private final OrderPricingService orderPricingService;

	@Transactional
	public OrderCreateResponse create(OrderCreateCommand command) {
		Preconditions.validate(
			command.memberId() != null,
			ErrorCode.AUTH_INVALID
		);

		validateDuplicateHoldIds(command.holdIds());

		GameScheduleEntity gameSchedule = gameScheduleRepository.findById(command.gameId())
			.orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

		List<SeatHoldEntity> holds = seatHoldRepository.findAllWithDetailsByIdIn(command.holdIds());
		validateHolds(command.holdIds(), holds, command.gameId(), command.memberId());
		validateOrderedSeats(command.gameId(), holds);

		OrderPricingResult pricingResult = orderPricingService.calculate(gameSchedule, holds);

		OrderEntity order = orderService.create(
			command.memberId(),
			gameSchedule,
			holds.size(),
			pricingResult.totalAmount()
		);

		orderHistoryService.create(
			order,
			command.ordererName(),
			command.ordererPhone(),
			command.ordererEmail()
		);

		createOrderItems(order, pricingResult.pricedHolds());

		return OrderCreateResponse.from(
			order.getId(),
			order.getOrderNumber(),
			order.getGameSchedule().getId(),
			order.getOrderStatus(),
			order.getTotalQuantity(),
			order.getTotalAmount()
		);
	}

	private void validateDuplicateHoldIds(List<UUID> holdIds) {
		Preconditions.validate(
			holdIds.size() == new HashSet<>(holdIds).size(),
			ErrorCode.DUPLICATE_HOLD_ID_REQUEST
		);
	}

	private void validateHolds(
		List<UUID> requestedHoldIds,
		List<SeatHoldEntity> holds,
		UUID gameId,
		UUID userId
	) {
		Preconditions.validate(
			holds.size() == requestedHoldIds.size(),
			ErrorCode.SEAT_HOLD_NOT_FOUND
		);

		for (SeatHoldEntity hold : holds) {
			Preconditions.validate(
				hold.getUserId().equals(userId),
				ErrorCode.AUTH_PERMISSION_DENIED
			);
			Preconditions.validate(
				hold.getGameSchedule().getId().equals(gameId),
				ErrorCode.SEAT_HOLD_GAME_MISMATCH
			);
			Preconditions.validate(
				hold.getStatus() == SeatHoldStatus.HOLDING,
				ErrorCode.SEAT_HOLD_STATUS_INVALID
			);
			Preconditions.validate(
				hold.getExpiredAt().isAfter(LocalDateTime.now()),
				ErrorCode.SEAT_HOLD_EXPIRED
			);
		}
	}

	private void validateOrderedSeats(
		UUID gameId,
		List<SeatHoldEntity> holds
	) {
		List<UUID> seatIds = holds.stream()
			.map(hold -> hold.getSeat().getId())
			.toList();

		Preconditions.validate(
			!orderItemRepository.existsOrderedSeats(
				gameId,
				seatIds,
				EnumSet.of(
					OrderItemStatus.RESERVED,
					OrderItemStatus.PAID,
					OrderItemStatus.CANCEL_FAILED
				)
			),
			ErrorCode.ORDER_SEAT_ALREADY_EXISTS
		);
	}

	private void createOrderItems(
		OrderEntity order,
		List<OrderPricingResult.PricedHold> pricedHolds
	) {
		for (OrderPricingResult.PricedHold pricedHold : pricedHolds) {
			orderItemService.create(
				order,
				pricedHold.hold().getSeat(),
				TicketType.ADULT,
				pricedHold.ticketPrice()
			);
		}
	}
}
