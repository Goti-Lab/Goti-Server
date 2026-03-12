package com.goti.order.service.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

	private final OrderRepository orderRepository;

	@Override
	@Transactional
	public OrderEntity create(
		UUID userId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	) {
		OrderEntity order = OrderEntity.create(
			generateOrderNumber(),
			userId,
			gameSchedule,
			totalQuantity,
			totalAmount
		);

		return orderRepository.save(order);
	}

	private String generateOrderNumber() {
		String tsidSuffix = TsidCreator.getTsid().toString();
		return "ORD" + "-" +
			LocalDate.now().format(ORDER_NUMBER_FORMATTER) +
			tsidSuffix.substring(tsidSuffix.length() - 6);
	}

}
