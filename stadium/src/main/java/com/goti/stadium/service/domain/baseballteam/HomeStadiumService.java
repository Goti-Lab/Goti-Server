package com.goti.stadium.service.domain.baseballteam;

import com.goti.stadium.constants.StadiumType;
import com.goti.stadium.domain.entity.stadium.HomeStadiumEntity;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.domain.entity.team.BaseballTeamEntity;

public interface HomeStadiumService {
	HomeStadiumEntity create(
		BaseballTeamEntity baseballTeam,
		StadiumEntity stadium,
		StadiumType type
	);
}
