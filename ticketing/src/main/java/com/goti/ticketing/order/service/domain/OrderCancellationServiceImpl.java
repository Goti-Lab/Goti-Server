package com.goti.ticketing.order.service.domain;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.order.repository.OrderCancellationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCancellationServiceImpl implements OrderCancellationService {
	private final OrderCancellationRepository orderCancellationRepository;

	@Override
	@Transactional
	public OrderCancellationEntity create(
		OrderEntity order,
		OrderCancellationRequestType requestType,
		UUID requestedBy,
		Integer refundAmountTotal,
		Integer feeAmountTotal,
		String idempotencyKey
	) {
		OrderCancellationEntity cancellation = OrderCancellationEntity.create(
			order,
			requestType,
			requestedBy,
			refundAmountTotal,
			feeAmountTotal,
			idempotencyKey
		);
		return orderCancellationRepository.save(cancellation);
	}

	@Override
	@Transactional
	public void validate(OrderCancellationEntity cancellation) {
		cancellation.validateRequest();
	}

	@Override
	@Transactional
	public void startRefund(OrderCancellationEntity cancellation) {
		cancellation.startRefund();
	}

	@Override
	@Transactional
	public void complete(OrderCancellationEntity cancellation) {
		cancellation.complete();
	}
}
