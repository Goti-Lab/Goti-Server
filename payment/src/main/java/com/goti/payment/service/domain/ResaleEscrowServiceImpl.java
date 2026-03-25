package com.goti.payment.service.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.goti.payment.constants.EscrowStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;

@Service
public class ResaleEscrowServiceImpl implements ResaleEscrowService {

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
}
