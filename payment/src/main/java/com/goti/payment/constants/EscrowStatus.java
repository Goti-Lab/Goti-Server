package com.goti.payment.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EscrowStatus {
	HOLDING("대기상태"),
	RELEASED("지급완료");

	private final String description;
}
