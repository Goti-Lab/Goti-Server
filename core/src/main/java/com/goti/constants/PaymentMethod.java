package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

	CARD("카드"),
	ACCOUNT_TRANSFER("계좌이체");

	private final String description;
}
