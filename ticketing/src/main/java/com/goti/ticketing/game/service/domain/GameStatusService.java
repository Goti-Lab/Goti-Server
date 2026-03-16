package com.goti.ticketing.game.service.domain;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.game.GameStatusEntity;

public interface GameStatusService {

	GameStatusEntity create(GameScheduleEntity gameSchedule);
}
