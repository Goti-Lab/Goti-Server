package com.goti.repository;

import com.goti.domain.entity.team.BaseballTeamEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BaseballTeamRepository extends JpaRepository<BaseballTeamEntity, UUID> {
}
