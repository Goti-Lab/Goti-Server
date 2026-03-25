package com.goti.resale.service.domain;

import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;

public interface ResaleRestrictionDomainService {
	ResaleRestrictionEntity getOrCreateRestriction(UUID userId);
}
