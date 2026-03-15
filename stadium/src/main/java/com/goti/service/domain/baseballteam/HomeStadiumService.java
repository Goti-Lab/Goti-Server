package com.goti.service.domain.baseballteam;

import com.goti.constants.StadiumType;
import com.goti.domain.entity.stadium.HomeStadiumEntity;
import com.goti.domain.entity.stadium.StadiumEntity;
import com.goti.domain.entity.team.BaseballTeamEntity;

public interface HomeStadiumService {
	HomeStadiumEntity create(
		BaseballTeamEntity baseballTeam,
		StadiumEntity stadium,
		StadiumType type
	);
}
