package com.goti.ticketing.pricing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.pricing.TicketPricingPolicyEntity;

@Repository
public interface TicketPricingPolicyRepository extends JpaRepository<TicketPricingPolicyEntity, UUID> {
	@Query("""
		SELECT tpp
			FROM TicketPricingPolicyEntity tpp
		WHERE tpp.teamId = :teamId
		  AND tpp.isActive = true
		ORDER BY tpp.policyStartAt DESC
	""")
	Optional<TicketPricingPolicyEntity> findLatestActivePolicy(@Param("teamId") UUID teamId);
}
