package com.goti.payment.service.domain;

import java.util.List;

import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.request.ResalePaymentRequest;

public interface ResaleOrderPaymentService {
	List<EscrowAccountEntity> createEscrows(ResalePaymentRequest request);
}
