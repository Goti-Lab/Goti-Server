package com.goti.payment.service.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.goti.payment.domain.entity.payment.EscrowAccountEntity;

public interface ResaleEscrowService {
	List<EscrowAccountEntity> filterHoldings(List<EscrowAccountEntity> escrows);

	void settle(List<EscrowAccountEntity> escrows, LocalDateTime releaseTime);
}
