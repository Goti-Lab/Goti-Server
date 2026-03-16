package com.goti.game.service.domain;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.GameTicketingStatusEntity;

public interface GameTicketingStatusService {

	GameTicketingStatusEntity create(GameScheduleEntity gameSchedule);
}
