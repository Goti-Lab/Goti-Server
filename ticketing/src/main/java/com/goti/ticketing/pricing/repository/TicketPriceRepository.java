package com.goti.ticketing.pricing.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.pricing.TicketPriceEntity;

@Repository
public interface TicketPriceRepository extends JpaRepository<TicketPriceEntity, UUID>, TicketPriceRepositoryCustom {
	List<TicketPriceEntity> findAllByPolicyId(UUID policyId);
}
