package com.goti.ticketing.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.TicketType;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.order.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
	private final OrderItemRepository orderItemRepository;

	@Override
	@Transactional
	public OrderItemEntity create(
		OrderEntity order,
		SeatEntity seat,
		TicketType ticketType,
		Integer ticketPrice
	) {
		OrderItemEntity orderItem = OrderItemEntity.create(order, seat, ticketType, ticketPrice);
		return orderItemRepository.save(orderItem);
	}
}
