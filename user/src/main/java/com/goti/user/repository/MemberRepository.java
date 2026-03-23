package com.goti.user.repository;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.user.domain.entity.user.MemberEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, UUID> {

	Optional<MemberEntity> findByMobile(String mobile);

	default MemberEntity findByIdOrThrow(UUID memberId) {
		return findById(memberId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
		);
	}
}
