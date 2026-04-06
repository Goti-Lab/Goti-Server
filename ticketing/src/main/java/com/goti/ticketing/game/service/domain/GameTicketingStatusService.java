package com.goti.ticketing.game.service.domain;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.game.GameTicketingStatusEntity;

public interface GameTicketingStatusService {

	GameTicketingStatusEntity create(GameScheduleEntity gameSchedule);

	GameTicketingStatusEntity getLocked(GameScheduleEntity gameSchedule);
}
