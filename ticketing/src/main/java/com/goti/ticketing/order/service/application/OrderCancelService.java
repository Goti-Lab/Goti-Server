package com.goti.ticketing.order.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.goti.ticketing.order.service.domain.OrderCancellationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.OrderStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.constants.GameResult;
import com.goti.ticketing.constants.GameStatus;
import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.constants.OrderItemStatus;
import com.goti.ticketing.constants.TicketStatus;
import com.goti.ticketing.infra.api.dto.response.PaymentCancelResponse;
import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.game.repository.GameStatusRepository;
import com.goti.ticketing.game.service.application.GameTicketManagementService;
import com.goti.ticketing.infra.api.TicketPaymentApiClient;
import com.goti.ticketing.order.dto.request.OrderCancelRequest;
import com.goti.ticketing.order.dto.response.OrderCancelResponse;
import com.goti.ticketing.order.service.domain.OrderCancellationItemService;
import com.goti.ticketing.order.service.domain.OrderCancellationRefundPolicy;
import com.goti.ticketing.order.service.domain.OrderItemService;
import com.goti.ticketing.order.service.domain.OrderService;
import com.goti.ticketing.seat.service.domain.SeatStatusService;
import com.goti.ticketing.ticket.service.domain.TicketFreezeInfo;
import com.goti.ticketing.ticket.service.domain.TicketFreezeService;
import com.goti.ticketing.ticket.service.domain.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelService {
	private static final int BOOKING_FEE_PER_ITEM = 1000;

	private final OrderService orderService;
	private final OrderItemService orderItemService;
	private final OrderCancellationService orderCancellationService;
	private final OrderCancellationItemService orderCancellationItemService;
	private final OrderCancellationRefundPolicy refundPolicy;
	private final TicketService ticketService;
	private final TicketFreezeService ticketFreezeService;
	private final SeatStatusService seatStatusService;
	private final TicketPaymentApiClient ticketPaymentApiClient;
	private final GameStatusRepository gameStatusRepository;
	private final GameTicketManagementService gameTicketManagementService;

	@Transactional
	public OrderCancelResponse cancel(
		UUID orderId,
		UUID memberId,
		OrderCancelRequest request
	) {
		Preconditions.validate(memberId != null, ErrorCode.AUTH_INVALID);

		OrderEntity order = orderService.get(orderId, memberId);
		validateOrderCancelable(order);

		List<OrderItemEntity> orderItems = orderItemService.get(orderId);
		List<OrderItemEntity> targetItems = resolveTargetItems(orderItems, request);
		validateTargetItems(targetItems, request.requestType());

		Map<UUID, TicketEntity> ticketMap = ticketService.getByOrderItemIds(
			targetItems.stream().map(OrderItemEntity::getId).toList()
		);
		validateTickets(ticketMap, targetItems);

		boolean refundableByGameCanceled = isRefundableByGameCanceled(order);
		int ticketAmount = targetItems.stream()
			.mapToInt(OrderItemEntity::getTicketPrice)
			.sum();
		int bookingFeeAmount = targetItems.size() * BOOKING_FEE_PER_ITEM;

		OrderCancellationRefundPolicy.RefundAmount refundAmount = refundPolicy.calculate(
			order.getConfirmedAt(),
			order.getGameSchedule().getStartAt(),
			LocalDateTime.now(),
			ticketAmount,
			bookingFeeAmount,
			request.requestType(),
			refundableByGameCanceled
		);

		OrderCancellationEntity cancellation = orderCancellationService.create(
			order,
			request.requestType(),
			memberId,
			refundAmount.refundAmount(),
			refundAmount.cancellationFeeAmount(),
			request.idempotencyKey()
		);

		orderCancellationService.validate(cancellation);
		orderCancellationService.startRefund(cancellation);

		for (OrderItemEntity targetItem : targetItems) {
			OrderCancellationRefundPolicy.RefundAmount itemRefundAmount = refundPolicy.calculate(
				order.getConfirmedAt(),
				order.getGameSchedule().getStartAt(),
				LocalDateTime.now(),
				targetItem.getTicketPrice(),
				0,
				request.requestType(),
				refundableByGameCanceled
			);
			orderCancellationItemService.create(
				cancellation,
				targetItem,
				itemRefundAmount.refundAmount(),
				itemRefundAmount.cancellationFeeAmount()
			);

			TicketEntity ticket = ticketMap.get(targetItem.getId());
			ticketService.invalidate(ticket);
			seatStatusService.cancelSale(
				seatStatusService.get(order.getGameSchedule(), targetItem.getSeat())
			);
			orderItemService.cancel(targetItem);
		}

		gameTicketManagementService.processRestoreAvailable(order.getGameSchedule());
		updateOrderStatus(order, orderItems);
		PaymentCancelResponse paymentData = ticketPaymentApiClient.cancelPayment(orderId, cancellation.getId());
		orderCancellationService.complete(cancellation);

		log.info(
			"action=ORDER_CANCEL gameId={} userId={} orderId={} cancelledItems={} refundAmount={}",
			order.getGameSchedule().getId(),
			memberId,
			orderId,
			targetItems.size(),
			refundAmount.refundAmount()
		);

		return OrderCancelResponse.from(
			cancellation,
			order,
			bookingFeeAmount - refundAmount.refundedBookingFeeAmount(),
			paymentData.paymentStatus(),
			paymentData.paymentMethod(),
			paymentData.paymentType(),
			targetItems.size()
		);
	}

	private void validateOrderCancelable(OrderEntity order) {
		Preconditions.validate(
			order.getOrderStatus() == OrderStatus.CONFIRMED
				|| order.getOrderStatus() == OrderStatus.PARTIALLY_CANCELED,
			ErrorCode.ORDER_CANCELLATION_NOT_ALLOWED
		);
	}

	private List<OrderItemEntity> resolveTargetItems(
		List<OrderItemEntity> orderItems,
		OrderCancelRequest request
	) {
		if (request.requestType() == OrderCancellationRequestType.ORDER_FULL) {
			return orderItems.stream()
				.filter(orderItem -> orderItem.getItemStatus() == OrderItemStatus.PAID)
				.toList();
		}

		Preconditions.validate(
			request.orderItemIds() != null && !request.orderItemIds().isEmpty(),
			ErrorCode.ORDER_CANCELLATION_ITEMS_REQUIRED
		);

		Set<UUID> targetIds = Set.copyOf(request.orderItemIds());
		return orderItems.stream()
			.filter(orderItem -> targetIds.contains(orderItem.getId()))
			.toList();
	}

	private void validateTargetItems(
		List<OrderItemEntity> targetItems,
		OrderCancellationRequestType requestType
	) {
		Preconditions.validate(
			!targetItems.isEmpty(),
			requestType == OrderCancellationRequestType.ORDER_PARTIAL
				? ErrorCode.ORDER_CANCELLATION_ITEMS_REQUIRED
				: ErrorCode.ORDER_CANCELLATION_NOT_ALLOWED
		);
		Preconditions.validate(
			targetItems.stream().allMatch(item -> item.getItemStatus() == OrderItemStatus.PAID),
			ErrorCode.ORDER_CANCELLATION_ITEM_INVALID
		);
	}

	private void validateTickets(
		Map<UUID, TicketEntity> ticketMap,
		List<OrderItemEntity> targetItems
	) {
		for (OrderItemEntity targetItem : targetItems) {
			TicketEntity ticket = ticketMap.get(targetItem.getId());
			if (ticket == null) {
				throw new CustomException(ErrorCode.TICKET_NOT_FOUND);
			}
			Preconditions.validate(
				ticket.getTicketStatus() != TicketStatus.USED,
				ErrorCode.TICKET_ALREADY_USED
			);

			TicketFreezeInfo currentFreeze = ticketFreezeService.getCurrentFreeze(ticket.getId());
			if (currentFreeze != null) {
				throw new CustomException(
					ErrorCode.TICKET_CANCELLATION_BLOCKED_BY_FREEZE,
					currentFreeze.freezeReason().getDescription() + " 사유로 동결된 티켓은 취소할 수 없습니다."
				);
			}
		}
	}

	private boolean isRefundableByGameCanceled(OrderEntity order) {
		return gameStatusRepository.findByGameSchedule(order.getGameSchedule())
			.map(gameStatus -> gameStatus.getGameStatus() == GameStatus.CANCELLED
				|| gameStatus.getGameStatus() == GameStatus.RAIN_CANCELLED
				|| gameStatus.getGameResult() == GameResult.CANCELLED)
			.orElse(false);
	}

	private void updateOrderStatus(OrderEntity order, List<OrderItemEntity> orderItems) {
		boolean hasPaidItem = orderItems.stream()
			.anyMatch(item -> item.getItemStatus() == OrderItemStatus.PAID);

		if (hasPaidItem) {
			orderService.partialCancel(order);
			return;
		}

		orderService.cancel(order);
	}
}
