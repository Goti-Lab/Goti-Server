package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EscrowStatus {
	HOLDING("대기중"),
	RELEASED("정산됨");

	private final String description;
}
