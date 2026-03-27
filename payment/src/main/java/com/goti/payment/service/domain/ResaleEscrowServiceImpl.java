package com.goti.payment.service.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.goti.payment.constants.EscrowStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.request.ResalePaymentRequest;
import com.goti.payment.infra.ResaleEscrowClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleEscrowServiceImpl implements ResaleEscrowService {
	private final ResaleEscrowClient escrowClient;

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

	@Override
	public void requestEscrowPayments(List<EscrowAccountEntity> escrows) {
		for (EscrowAccountEntity escrow : escrows) {
			String externalId = escrowClient.requestEscrowPayment(
				escrow.getTransactionId(),
				escrow.getEscrowAmount()
			);
			escrow.updateExternalId(externalId);
		}
	}

	@Override
	public List<EscrowAccountEntity> filterHoldings(List<EscrowAccountEntity> escrows) {
		return escrows.stream()
			.filter(escrow -> escrow.getEscrowStatus() == EscrowStatus.HOLDING)
			.toList();
	}

	@Override
	public void settle(List<EscrowAccountEntity> escrows, LocalDateTime releaseTime) {
		for (EscrowAccountEntity escrow : escrows) {
			escrow.settle(releaseTime);
		}
	}

	@Override
	public void requestSettlements(List<EscrowAccountEntity> escrows) {
		for (EscrowAccountEntity escrow : escrows) {
			if (escrow.getExternalEscrowId() != null) {
				escrowClient.requestSettlement(escrow.getExternalEscrowId());
			}
		}
	}
}