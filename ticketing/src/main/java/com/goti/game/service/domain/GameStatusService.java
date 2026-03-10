package com.goti.game.service.domain;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.GameStatusEntity;

public interface GameStatusService {

	GameStatusEntity create(GameScheduleEntity gameSchedule);
}
