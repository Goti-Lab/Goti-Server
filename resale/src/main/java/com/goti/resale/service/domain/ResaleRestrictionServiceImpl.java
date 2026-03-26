package com.goti.resale.service.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.repository.ResaleRestrictionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleRestrictionServiceImpl implements ResaleRestrictionService {

	private final ResaleRestrictionRepository restrictionRepository;

	@Override
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

	@Override
	@Transactional
	public Map<UUID, ResaleRestrictionEntity> getOrCreateRestrictions(List<UUID> userIds) {
		List<ResaleRestrictionEntity> existingRestrictions = restrictionRepository.findAllByUserIdIn(userIds);
		Map<UUID, ResaleRestrictionEntity> restrictionMap = existingRestrictions.stream()
			.collect(Collectors.toMap(ResaleRestrictionEntity::getUserId, restriction -> restriction));

		List<UUID> missingUserIds = userIds.stream()
			.filter(id -> !restrictionMap.containsKey(id))
			.distinct()
			.toList();

		if (!missingUserIds.isEmpty()) {
			List<ResaleRestrictionEntity> newRestrictions = missingUserIds.stream()
				.map(ResaleRestrictionEntity::create)
				.toList();

			try {
				List<ResaleRestrictionEntity> saved = restrictionRepository.saveAllAndFlush(newRestrictions);
				saved.forEach(r -> restrictionMap.put(r.getUserId(), r));
			} catch (DataIntegrityViolationException e) {
				missingUserIds.forEach(id -> restrictionMap.put(id, getOrCreateRestriction(id)));
			}
		}

		return restrictionMap;
	}
}
