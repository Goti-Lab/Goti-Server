package com.goti.resale.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EscrowStatus {
	HOLDING("대기상태"),
	RELEASED("지급완료");

	private final String description;
}
