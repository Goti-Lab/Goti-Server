package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserStatus {
	ACTIVATED("활성화"),
	SUSPENDED("정지상태"),
	DELETED("삭제상태");

	private final String description;
}
