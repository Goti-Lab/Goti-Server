package com.goti.payment.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {

	PAYMENT("결제"),
	REFUND("환불");

	private final String description;
}
