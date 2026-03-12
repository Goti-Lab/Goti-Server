package com.goti.pricing.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.domain.entity.pricing.TicketPriceEntity;

@Repository
public interface TicketPriceRepository extends JpaRepository<TicketPriceEntity, UUID>, TicketPriceRepositoryCustom {
}
