package com.goti.repository;

import com.goti.domain.entity.user.MemberEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, UUID> {

	Optional<MemberEntity> findByMobile(String mobile);

}
