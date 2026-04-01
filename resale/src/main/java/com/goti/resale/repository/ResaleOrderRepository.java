package com.goti.resale.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.resale.domain.entity.resale.ResaleOrderEntity;

public interface ResaleOrderRepository extends JpaRepository<ResaleOrderEntity, UUID>, ResaleOrderRepositoryCustom {
}
