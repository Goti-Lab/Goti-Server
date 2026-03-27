package com.goti.payment.service.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.payment.infra.ResaleOrderClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleServiceImpl implements ResaleService {
	private final ResaleOrderClient resaleOrderClient;

	@Override
	public void confirmOrder(UUID orderId, UUID paymentId) {
		resaleOrderClient.completeOrder(orderId, paymentId);
	}

	@Override
	public List<UUID> getTransactionIds(UUID orderId) {
		return resaleOrderClient.getTransactionIds(orderId);
	}
}
