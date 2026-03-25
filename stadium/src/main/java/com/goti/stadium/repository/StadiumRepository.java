package com.goti.stadium.repository;

import com.goti.constants.messages.ErrorCode;
import com.goti.stadium.domain.entity.stadium.StadiumEntity;

import com.goti.exception.CustomException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StadiumRepository extends JpaRepository<StadiumEntity, UUID> {

	default StadiumEntity findByIdOrThrow(UUID stadiumId) {
		return findById(stadiumId).orElseThrow(
			() -> new CustomException(ErrorCode.STADIUM_NOT_FOUND)
		);
	}

	List<StadiumEntity> findAllByIdIn(List<UUID> stadiumIds);
}
