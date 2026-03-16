package com.goti.stadium.service.application;

import com.goti.stadium.constants.StadiumType;
import com.goti.stadium.domain.entity.stadium.HomeStadiumEntity;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.domain.entity.team.BaseballTeamEntity;
import com.goti.stadium.dto.response.HomeStadiumCreateResponse;
import com.goti.stadium.service.domain.baseballteam.BaseballTeamService;
import com.goti.stadium.service.domain.baseballteam.HomeStadiumService;
import com.goti.stadium.service.domain.stadium.StadiumService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HomeStadiumManagementService {

	private final BaseballTeamService baseballTeamService;
	private final StadiumService stadiumService;
	private final HomeStadiumService homeStadiumService;

	@Transactional
	public HomeStadiumCreateResponse assignHomeStadium(
		UUID teamId, UUID stadiumId, StadiumType type
	) {
		BaseballTeamEntity baseballTeam = baseballTeamService.getById(teamId);
		StadiumEntity stadium = stadiumService.getById(stadiumId);
		HomeStadiumEntity homeStadium = homeStadiumService.create(
			baseballTeam, stadium, type
		);
		return HomeStadiumCreateResponse.from(
			homeStadium.getId(),
			baseballTeam.getId(),
			stadium.getId(),
			baseballTeam.getTeamCode(),
			baseballTeam.getTeamName(),
			baseballTeam.getTeamNameEn(),
			stadium.getStadiumName(),
			stadium.getLocation(),
			homeStadium.getType()
		);
	}

}
