package com.goti.resale.service.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;

public interface ResaleRestrictionService {
	ResaleRestrictionEntity getOrCreateRestriction(UUID userId);

	Map<UUID, ResaleRestrictionEntity> getOrCreateRestrictions(List<UUID> userIds);
}
