package com.goti.game.repository;

import com.goti.domain.entity.game.GameTicketingStatusEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameTicketingStatusRepository extends JpaRepository<GameTicketingStatusEntity, UUID> {
}
