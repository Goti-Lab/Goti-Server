package com.goti.payment.service.domain;

import java.util.List;

import org.springframework.stereotype.Service;

import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.request.ResalePaymentRequest;

@Service
public class ResaleOrderPaymentServiceImpl implements ResaleOrderPaymentService {

	@Override
	public List<EscrowAccountEntity> createEscrows(ResalePaymentRequest request) {
		return request.items()
			.stream()
			.map(item ->
				EscrowAccountEntity
					.create(
						item.transactionId(),
						request.buyerId(),
						item.sellerId(),
						item.settlementAmount()
					))
			.toList();
	}
}
