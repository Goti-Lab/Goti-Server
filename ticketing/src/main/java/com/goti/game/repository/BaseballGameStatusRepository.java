package com.goti.game.repository;

import com.goti.domain.entity.game.BaseballGameStatusEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BaseballGameStatusRepository extends JpaRepository<BaseballGameStatusEntity, UUID> {
}
