package com.goti.ticketing.infra.api.dto.response;

import java.util.UUID;

public record BaseballTeamDisplayNameResponse(
	UUID teamId,
	String teamDisplayName
) {
}
