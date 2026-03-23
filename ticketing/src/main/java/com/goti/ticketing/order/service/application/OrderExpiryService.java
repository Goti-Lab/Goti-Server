package com.goti.ticketing.order.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.ticketing.order.service.domain.OrderItemService;
import com.goti.ticketing.order.service.domain.OrderService;
import com.goti.ticketing.seat.service.domain.SeatHoldExpiryService;
import com.goti.ticketing.seat.service.domain.SeatStatusService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderExpiryService {
	private final OrderService orderService;
	private final OrderItemService orderItemService;
	private final SeatHoldExpiryService seatHoldExpiryService;
	private final SeatStatusService seatStatusService;

	@Transactional
	public void expire(UUID orderId) {
		OrderEntity order = orderService.get(orderId);
		orderService.expire(order);

		List<OrderItemEntity> orderItems = orderItemService.get(orderId);
		List<UUID> holdIds = orderItems.stream()
			.map(OrderItemEntity::getHoldId)
			.toList();
		List<UUID> seatIds = orderItems.stream()
			.map(orderItem -> orderItem.getSeat().getId())
			.toList();

		Map<UUID, SeatHoldEntity> seatHoldMap = seatHoldExpiryService.getByIds(holdIds);
		Map<UUID, SeatStatusEntity> seatStatusMap = seatStatusService.getByGameIdAndSeatIds(
			order.getGameSchedule().getId(),
			seatIds
		);

		for (OrderItemEntity orderItem : orderItems) {
			SeatHoldEntity seatHold = seatHoldMap.get(orderItem.getHoldId());
			Preconditions.validate(
				seatHold != null,
				ErrorCode.SEAT_HOLD_NOT_FOUND
			);

			SeatStatusEntity seatStatus = seatStatusMap.get(orderItem.getSeat().getId());
			Preconditions.validate(
				seatStatus != null,
				ErrorCode.SEAT_STATUS_NOT_FOUND
			);

			seatHoldExpiryService.expire(seatStatus, seatHold, LocalDateTime.now());
			orderItemService.expire(orderItem);
		}
	}
}
