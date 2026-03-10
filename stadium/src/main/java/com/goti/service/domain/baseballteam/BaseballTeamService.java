package com.goti.service.domain.baseballteam;

import com.goti.constants.TeamCode;
import com.goti.dto.response.BaseballTeamCreateResponse;

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
}
