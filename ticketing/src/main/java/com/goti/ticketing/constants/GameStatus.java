package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameStatus {

	SCHEDULED("경기 예정"),
	IN_PROGRESS("경기 진행중"),
	SUSPENDED("경기 중단"),
	RAIN_DELAY("우천 중단"),
	RAIN_CANCELLED("우천 취소"),
	CANCELLED("경기 취소"),
	FINISHED("경기 종료");

	private final String description;
}