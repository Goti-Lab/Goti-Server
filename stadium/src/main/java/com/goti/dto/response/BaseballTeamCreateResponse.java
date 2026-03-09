package com.goti.dto.response;

import com.goti.constants.TeamCode;

import java.util.UUID;

public record BaseballTeamCreateResponse(
	UUID teamId,
	TeamCode teamCode,
	String teamName,
	String teamNameEn,
	String homeGround,
	String owner,
	String director
) {
	public static BaseballTeamCreateResponse from(
		UUID teamId,
		TeamCode teamCode,
		String teamName,
		String teamNameEn,
		String homeGround,
		String owner,
		String director
	) {
		return new BaseballTeamCreateResponse(
			teamId, teamCode, teamName, teamNameEn, homeGround, owner, director
		);
	}
}
