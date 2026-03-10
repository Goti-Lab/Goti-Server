package com.goti.order.service.domain;

import com.goti.domain.entity.order.OrderHistoryEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.domain.entity.order.OrderEntity;
import com.goti.order.repository.OrderHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {
	private final OrderHistoryRepository orderHistoryRepository;

	@Override
	@Transactional
	public OrderHistoryEntity create(
		OrderEntity order,
		String name,
		String phone,
		String email
	) {
		OrderHistoryEntity orderer = OrderHistoryEntity.create(order, name, phone, email);
		return orderHistoryRepository.save(orderer);
	}
}
