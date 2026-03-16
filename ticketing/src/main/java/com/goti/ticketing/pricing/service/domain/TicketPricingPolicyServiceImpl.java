package com.goti.ticketing.pricing.service.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketPricingMatchType;
import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.pricing.TicketPriceEntity;
import com.goti.ticketing.domain.entity.pricing.TicketPricingPolicyEntity;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.pricing.dto.response.TicketPricingPolicyCreateResponse;
import com.goti.ticketing.pricing.repository.TicketPriceRepository;
import com.goti.ticketing.pricing.repository.TicketPricingPolicyRepository;
import com.goti.ticketing.seat.repository.SeatGradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketPricingPolicyServiceImpl implements TicketPricingPolicyService {
	private final TicketPricingPolicyRepository ticketPricingPolicyRepository;
	private final TicketPriceRepository ticketPriceRepository;
	private final SeatGradeRepository seatGradeRepository;

	@Override
	@Transactional
	public TicketPricingPolicyCreateResponse create(
		UUID teamId,
		LocalDate policyStartAt,
		LocalDate policyEndAt,
		List<TicketPriceCreateParam> ticketPriceRequest
	) {
		TicketPricingPolicyEntity policy = TicketPricingPolicyEntity.create(
			teamId,
			policyStartAt,
			policyEndAt
		);

		ticketPricingPolicyRepository.save(policy);

		Map<UUID, SeatGradeEntity> gradesById = getGrades(ticketPriceRequest);
		validateDuplicateticketPrice(ticketPriceRequest);

		List<TicketPriceEntity> ticketPrices = ticketPriceRequest.stream()
			.map(price -> TicketPriceEntity.create(
				gradesById.get(price.gradeId()),
				policy,
				price.ticketType(),
				price.dayType(),
				price.matchType(),
				price.price()
			))
			.toList();

		ticketPriceRepository.saveAll(ticketPrices);
		return TicketPricingPolicyCreateResponse.from(policy, ticketPrices);
	}

	private Map<UUID, SeatGradeEntity> getGrades(List<TicketPriceCreateParam> ticketPriceRequest) {
		List<UUID> gradeIds = ticketPriceRequest.stream()
			.map(TicketPriceCreateParam::gradeId)
			.distinct()
			.toList();

		List<SeatGradeEntity> grades = seatGradeRepository.findAllById(gradeIds);
		Preconditions.validate(
			grades.size() == gradeIds.size(),
			ErrorCode.SEAT_GRADE_NOT_FOUND
		);

		return grades.stream()
			.collect(Collectors.toMap(SeatGradeEntity::getId, Function.identity()));
	}

	private void validateDuplicateticketPrice(
		List<TicketPriceCreateParam> ticketPriceRequest
	) {
		Set<TicketPriceCondition> uniqueConditions = new HashSet<>();

		for (TicketPriceCreateParam price : ticketPriceRequest) {
			Preconditions.validate(
				uniqueConditions.add(
					new TicketPriceCondition(
						price.gradeId(),
						price.dayType(),
						price.matchType()
					)
				),
				ErrorCode.TICKET_PRICE_CONDITION_ALREADY_EXISTS
			);
		}
	}

	private record TicketPriceCondition(
		UUID gradeId,
		TicketPricingDayType dayType,
		TicketPricingMatchType matchType
	) {
	}
}
