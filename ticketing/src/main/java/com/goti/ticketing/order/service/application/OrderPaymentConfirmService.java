package com.goti.ticketing.order.service.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.OrderStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.order.dto.response.OrderPaymentConfirmResponse;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.order.repository.OrderRepository;
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
	private final TicketCreateService ticketCreateService;

	@Transactional
	public OrderPaymentConfirmResponse confirm(
		UUID orderId,
		UUID userId,
		UUID paymentId,
		String pgTid
	) {
		log.info(
			"주문 결제 완료 처리 시작 - orderId: {}, userId: {}, paymentId: {}, pgTid: {}",
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
			orderItem.pay();
		}

		List<TicketResponse> tickets = ticketCreateService.create(order);

		return OrderPaymentConfirmResponse.from(
			order.getId(),
			order.getOrderStatus(),
			tickets.size()
		);
	}
}
