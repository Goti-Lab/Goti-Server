package com.goti.resale.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;

public interface ResaleTransactionRepository extends JpaRepository<ResaleTransactionEntity, UUID> {

}
