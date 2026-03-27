package com.goti.payment.service.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.request.ResalePaymentRequest;

public interface ResaleEscrowService {

	List<EscrowAccountEntity> createEscrows(ResalePaymentRequest request);

	void requestEscrowPayments(List<EscrowAccountEntity> escrows);

	List<EscrowAccountEntity> filterHoldings(List<EscrowAccountEntity> escrows);

	void settle(List<EscrowAccountEntity> escrows, LocalDateTime releaseTime);

	void requestSettlements(List<EscrowAccountEntity> escrows);
}
