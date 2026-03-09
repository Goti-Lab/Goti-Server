package com.goti.service.domain.baseballteam;

import com.goti.constants.TeamCode;
import com.goti.domain.entity.team.BaseballTeamEntity;
import com.goti.dto.response.BaseballTeamCreateResponse;

import com.goti.repository.BaseballTeamRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BaseballTeamServiceImpl implements BaseballTeamService {

	private final BaseballTeamRepository baseballTeamRepository;

	@Override
	@Transactional
	public BaseballTeamCreateResponse create(
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
	) {
		BaseballTeamEntity baseballTeam = BaseballTeamEntity.create(
			teamCode, teamName, teamNameEn, sponsor, homeGround, foundedYear, officeAddress,
			zipCode, siteAddress, owner, ownerAgency, ceo, generalManager, director, logoUrl
		);
		baseballTeamRepository.save(baseballTeam);

		return BaseballTeamCreateResponse.from(
			baseballTeam.getId(),
			baseballTeam.getTeamCode(),
			baseballTeam.getTeamName(),
			baseballTeam.getTeamNameEn(),
			baseballTeam.getHomeGround(),
			baseballTeam.getOwner(),
			baseballTeam.getDirector()
		);
	}
}

