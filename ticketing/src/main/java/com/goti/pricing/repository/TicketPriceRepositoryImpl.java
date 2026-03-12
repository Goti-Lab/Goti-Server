package com.goti.pricing.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.constants.TicketPricingDayType;
import com.goti.constants.TicketPricingMatchType;
import com.goti.constants.TicketType;
import com.goti.domain.entity.pricing.QTicketPriceEntity;
import com.goti.domain.entity.pricing.QTicketPricingPolicyEntity;
import com.goti.domain.entity.pricing.TicketPriceEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TicketPriceRepositoryImpl implements TicketPriceRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<TicketPriceEntity> findApplicableTicketPrice(
		UUID teamId,
		LocalDate playDate,
		SeatGradeEntity grade,
		TicketType ticketType,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType
	) {
		QTicketPriceEntity ticketPrice = QTicketPriceEntity.ticketPriceEntity;
		QTicketPricingPolicyEntity policy = QTicketPricingPolicyEntity.ticketPricingPolicyEntity;

		return Optional.ofNullable(
			queryFactory
				.selectFrom(ticketPrice)
				.join(ticketPrice.policy, policy).fetchJoin()
				.join(ticketPrice.grade).fetchJoin()
				.where(
					policy.teamId.eq(teamId),
					policy.isActive.isTrue(),
					policy.policyStartAt.loe(playDate),
					policy.policyEndAt.goe(playDate),
					ticketPrice.grade.eq(grade),
					ticketPrice.ticketType.eq(ticketType),
					ticketPrice.dayType.eq(dayType),
					ticketPrice.matchType.eq(matchType)
				)
				.orderBy(policy.policyStartAt.desc())
				.fetchOne()
		);
	}
}
