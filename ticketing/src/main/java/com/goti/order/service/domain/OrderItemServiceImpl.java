package com.goti.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.TicketType;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.domain.entity.order.OrderItemEntity;
import com.goti.domain.entity.seat.SeatEntity;
import com.goti.order.repository.OrderItemRepository;

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
