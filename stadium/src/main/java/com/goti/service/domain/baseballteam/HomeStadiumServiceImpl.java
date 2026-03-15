package com.goti.service.domain.baseballteam;

import com.goti.constants.StadiumType;
import com.goti.domain.entity.stadium.HomeStadiumEntity;
import com.goti.domain.entity.stadium.StadiumEntity;
import com.goti.domain.entity.team.BaseballTeamEntity;

import com.goti.repository.HomeStadiumRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeStadiumServiceImpl implements HomeStadiumService {

	private final HomeStadiumRepository homeStadiumRepository;

	@Override
	@Transactional
	public HomeStadiumEntity create(
		BaseballTeamEntity baseballTeam, StadiumEntity stadium, StadiumType type
	) {
		HomeStadiumEntity homeStadium = HomeStadiumEntity.create(
			baseballTeam, stadium, type
		);
		return homeStadiumRepository.save(homeStadium);
	}
}
