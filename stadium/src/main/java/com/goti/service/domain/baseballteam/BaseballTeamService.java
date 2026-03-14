package com.goti.service.domain.baseballteam;

import com.goti.domain.entity.team.BaseballTeamEntity;
import com.goti.dto.response.BaseballTeamCreateResponse;
import com.goti.service.domain.baseballteam.command.BaseballTeamCreateCommand;

import java.util.UUID;

public interface BaseballTeamService {

	BaseballTeamCreateResponse create(BaseballTeamCreateCommand command);

	BaseballTeamEntity getById(UUID teamId);
}
