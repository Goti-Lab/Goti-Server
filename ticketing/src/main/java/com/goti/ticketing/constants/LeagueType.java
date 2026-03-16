package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeagueType {

	PRE_SEASON("프리시즌"),
	REGULAR("정규리그"),
	POSTSEASON("포스트시즌");

	private final String description;
}