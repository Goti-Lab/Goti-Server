package com.goti.ticketing.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderCancellationItemEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.order.repository.OrderCancellationItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCancellationItemServiceImpl implements OrderCancellationItemService {
	private final OrderCancellationItemRepository orderCancellationItemRepository;

	@Override
	@Transactional
	public OrderCancellationItemEntity create(
		OrderCancellationEntity cancellation,
		OrderItemEntity item,
		Integer refundAmount,
		Integer feeAmount
	) {
		OrderCancellationItemEntity cancellationItem = OrderCancellationItemEntity.create(
			cancellation,
			item,
			refundAmount,
			feeAmount
		);
		return orderCancellationItemRepository.save(cancellationItem);
	}
}
