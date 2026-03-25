package com.goti.payment.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.request.ResalePaymentRequest;

public interface ResaleOrderPaymentDomainService {
	List<EscrowAccountEntity> createEscrows(ResalePaymentRequest request);
}
