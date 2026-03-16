package com.goti.payment.service.dto;

import java.util.UUID;

import com.goti.constants.OrderStatus;

public record PaymentOrderInfo(
	UUID orderId,
	UUID memberId,
	OrderStatus orderStatus,
	Integer totalAmount
) {
}
