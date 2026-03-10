package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCancellationRequestType {

	USER_PARTIAL("부분 취소"),
	USER_FULL("전체 취소"),
	GAME_CANCELED("경기 취소"),
	SCHEDULE_CHANGED("경기 일정 변경");

	private final String description;
}
