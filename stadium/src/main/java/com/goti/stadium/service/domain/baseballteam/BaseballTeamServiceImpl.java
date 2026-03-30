package com.goti.stadium.service.domain.baseballteam;

import com.goti.stadium.domain.entity.team.BaseballTeamEntity;
import com.goti.stadium.dto.response.BaseballTeamCreateResponse;

import com.goti.stadium.dto.response.internal.BaseballTeamDisplayNameResponse;
import com.goti.stadium.repository.BaseballTeamRepository;

import com.goti.stadium.service.domain.baseballteam.command.BaseballTeamCreateCommand;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
			command.displayName(),
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

	@Override
	public List<BaseballTeamDisplayNameResponse> getDisplayNamesByIds(List<UUID> teamIds) {
		return baseballTeamRepository.findAllByIdIn(teamIds)
			.stream()
			.map(BaseballTeamDisplayNameResponse::from)
			.toList();
	}
}

