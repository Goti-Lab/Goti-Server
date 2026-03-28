package com.goti.stadium.dto.response.internal;

import com.goti.stadium.domain.entity.stadium.StadiumEntity;

import java.util.UUID;

public record StadiumLocationResponse(
	UUID stadiumId,
	String stadiumLocation
) {
	public static StadiumLocationResponse from(StadiumEntity stadium) {
		return new StadiumLocationResponse(
			stadium.getId(), stadium.getLocation()
		);
	}
}
