package com.goti.resale.service.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.repository.ResaleRestrictionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleRestrictionService {

	private final ResaleRestrictionRepository restrictionRepository;

	@Transactional
	public ResaleRestrictionEntity getOrCreateRestriction(UUID userId) {
		return restrictionRepository
			.findByUserId(userId)
			.orElseGet(() -> {
				try {
					ResaleRestrictionEntity newRestriction = ResaleRestrictionEntity.create(userId);
					return restrictionRepository.saveAndFlush(newRestriction);
				} catch (DataIntegrityViolationException e) {
					return restrictionRepository.findByUserId(userId)
						.orElseThrow(() -> new IllegalStateException("사용자 제한 정보 생성 중 오류가 발생했습니다."));
				}
			});
	}
}
