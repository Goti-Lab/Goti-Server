package com.goti.stadium.service.domain.baseballteam.command;

import com.goti.stadium.constants.TeamCode;

public record BaseballTeamCreateCommand(
	TeamCode teamCode,
	String displayName,
	String teamName,
	String teamNameEn,
	String sponsor,
	String homeGround,
	Integer foundedYear,
	String officeAddress,
	String zipCode,
	String siteAddress,
	String owner,
	String ownerAgency,
	String ceo,
	String generalManager,
	String director,
	String logoUrl
) {
}
