package com.goti.stadium.service.domain.baseballteam;

import com.goti.stadium.domain.entity.team.BaseballTeamEntity;
import com.goti.stadium.dto.response.BaseballTeamCreateResponse;
import com.goti.stadium.dto.response.internal.BaseballTeamDisplayNameResponse;
import com.goti.stadium.service.domain.baseballteam.command.BaseballTeamCreateCommand;

import java.util.List;
import java.util.UUID;

public interface BaseballTeamService {

	BaseballTeamCreateResponse create(BaseballTeamCreateCommand command);

	BaseballTeamEntity getById(UUID teamId);

	List<BaseballTeamDisplayNameResponse> getDisplayNamesByIds(List<UUID> teamIds);
}
