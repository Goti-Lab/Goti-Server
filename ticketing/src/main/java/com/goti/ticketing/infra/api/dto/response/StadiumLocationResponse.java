package com.goti.ticketing.infra.api.dto.response;

import java.util.UUID;

public record StadiumLocationResponse(
	UUID stadiumId,
	String stadiumLocation
) {
}
