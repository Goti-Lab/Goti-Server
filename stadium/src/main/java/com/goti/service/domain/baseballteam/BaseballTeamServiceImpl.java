package com.goti.service.domain.baseballteam;

import com.goti.domain.entity.team.BaseballTeamEntity;
import com.goti.dto.response.BaseballTeamCreateResponse;

import com.goti.repository.BaseballTeamRepository;

import com.goti.service.domain.baseballteam.command.BaseballTeamCreateCommand;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BaseballTeamServiceImpl implements BaseballTeamService {

	private final BaseballTeamRepository baseballTeamRepository;

	@Override
	@Transactional
	public BaseballTeamCreateResponse create(BaseballTeamCreateCommand command) {
		BaseballTeamEntity baseballTeam = BaseballTeamEntity.create(
			command.teamCode(),
			command.teamName(),
			command.teamNameEn(),
			command.sponsor(),
			command.homeGround(),
			command.foundedYear(),
			command.officeAddress(),
			command.zipCode(),
			command.siteAddress(),
			command.owner(),
			command.ownerAgency(),
			command.ceo(),
			command.generalManager(),
			command.director(),
			command.logoUrl()
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

	@Override
	public BaseballTeamEntity getById(UUID teamId) {
		return baseballTeamRepository.findByIdOrThrow(teamId);
	}
}

