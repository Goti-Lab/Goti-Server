package com.goti.resale.infra;

import java.util.UUID;

import com.goti.resale.dto.request.ResalePaymentRequest;

public interface PaymentClient {
	void createResalePayment(ResalePaymentRequest request);

	void releaseEscrow(UUID orderId);

}
