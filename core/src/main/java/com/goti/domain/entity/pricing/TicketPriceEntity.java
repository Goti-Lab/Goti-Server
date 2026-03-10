package com.goti.domain.entity.pricing;

import static lombok.AccessLevel.*;

import java.util.UUID;

import com.goti.constants.TicketPricingDayType;
import com.goti.constants.TicketPricingMatchType;
import com.goti.constants.TicketType;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "ticket_prices",
	indexes = {
		@Index(name = "idx_ticket_prices_grade_id", columnList = "grade_id"),
		@Index(name = "idx_ticket_prices_policy_id", columnList = "policy_id")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class TicketPriceEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grade_id", nullable = false)
	private SeatGradeEntity grade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "policy_id", nullable = false)
	private TicketPricingPolicyEntity policy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketType ticketType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketPricingDayType dayType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketPricingMatchType matchType;

	@Column(nullable = false)
	private Integer price;

	private TicketPriceEntity(
		SeatGradeEntity grade,
		TicketPricingPolicyEntity policy,
		TicketType ticketType,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType,
		Integer price
	) {
		this.grade = grade;
		this.policy = policy;
		this.ticketType = ticketType;
		this.dayType = dayType;
		this.matchType = matchType;
		this.price = price;
	}

	public static TicketPriceEntity create(
		SeatGradeEntity grade,
		TicketPricingPolicyEntity policy,
		TicketType ticketType,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType,
		Integer price
	) {
		validate(ticketType, dayType, matchType, price);
		return new TicketPriceEntity(
			grade,
			policy,
			ticketType,
			dayType,
			matchType,
			price
		);
	}

	private static void validate(
		TicketType ticketType,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType,
		Integer price
	) {
		Preconditions.domainValidate(
			ticketType != null,
			"권종은 필수입니다."
		);
		Preconditions.domainValidate(
			dayType != null,
			"요일 유형은 필수입니다."
		);
		Preconditions.domainValidate(
			matchType != null,
			"매치 유형은 필수입니다."
		);
		Preconditions.domainValidate(
			price != null && price >= 0,
			"가격은 0 이상이어야 합니다."
		);
	}
}
