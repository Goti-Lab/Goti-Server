package com.goti.stadium.dto.response.internal;

import com.goti.stadium.domain.entity.team.BaseballTeamEntity;

import java.util.UUID;

public record BaseballTeamDisplayNameResponse(
	UUID teamId,
	String teamDisplayName
) {

	public static BaseballTeamDisplayNameResponse from(
		BaseballTeamEntity baseballTeam
	) {
		return new BaseballTeamDisplayNameResponse(
			baseballTeam.getId(),
			baseballTeam.getDisplayName()
		);
	}
}
