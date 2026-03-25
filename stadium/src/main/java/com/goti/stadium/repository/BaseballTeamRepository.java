package com.goti.stadium.repository;

import com.goti.constants.messages.ErrorCode;
import com.goti.stadium.domain.entity.team.BaseballTeamEntity;

import com.goti.exception.CustomException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BaseballTeamRepository extends JpaRepository<BaseballTeamEntity, UUID> {

	default BaseballTeamEntity findByIdOrThrow(UUID teamId) {
		return findById(teamId).orElseThrow(
			() -> new CustomException(ErrorCode.BASEBALL_TEAM_NOT_FOUND)
		);
	}

	List<BaseballTeamEntity> findAllByIdIn(List<UUID> ids);
}
