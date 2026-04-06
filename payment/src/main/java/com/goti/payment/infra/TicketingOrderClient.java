package com.goti.payment.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.payment.dto.response.TicketingOrderListItemResponse;
import com.goti.payment.service.dto.OrderPaymentConfirmApiRequest;
import com.goti.payment.service.dto.PaymentOrderInfo;

public interface TicketingOrderClient {
	void confirmPayment(UUID orderId, OrderPaymentConfirmApiRequest request);

	PaymentOrderInfo getPaymentOrder(UUID orderId, UUID memberId);

	List<TicketingOrderListItemResponse> getOrders(
		UUID memberId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	);
}