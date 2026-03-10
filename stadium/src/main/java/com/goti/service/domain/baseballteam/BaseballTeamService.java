package com.goti.service.domain.baseballteam;

import com.goti.constants.TeamCode;
import com.goti.domain.entity.team.BaseballTeamEntity;
import com.goti.dto.response.BaseballTeamCreateResponse;

import javax.swing.text.html.Option;

import java.util.Optional;
import java.util.UUID;

public interface BaseballTeamService {

	BaseballTeamCreateResponse create(
		TeamCode teamCode,
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
	);

	BaseballTeamEntity getById(UUID teamId);
}
