package com.goti.ticketing.pricing.service.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.goti.exception.CustomException;

import com.goti.ticketing.constants.LeagueType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.pricing.TicketPriceEntity;
import com.goti.ticketing.domain.entity.pricing.TicketPricingPolicyEntity;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.pricing.dto.response.TicketPricingPolicyCreateResponse;
import com.goti.ticketing.pricing.dto.response.TicketPricingPolicyResponse;
import com.goti.ticketing.pricing.repository.TicketPriceRepository;
import com.goti.ticketing.pricing.repository.TicketPricingPolicyRepository;
import com.goti.ticketing.pricing.service.domain.command.TicketPriceCreateCommand;
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
		List<TicketPriceCreateCommand> ticketPriceRequest
	) {
		TicketPricingPolicyEntity policy = TicketPricingPolicyEntity.create(
			teamId,
			policyStartAt,
			policyEndAt
		);

		ticketPricingPolicyRepository.save(policy);

		Map<UUID, SeatGradeEntity> gradesById = getGrades(ticketPriceRequest);
		validateDuplicateTicketPrice(ticketPriceRequest);

		List<TicketPriceEntity> ticketPrices = ticketPriceRequest.stream()
			.map(price -> TicketPriceEntity.create(
				gradesById.get(price.gradeId()),
				policy,
				price.ticketType(),
				price.dayType(),
				price.leagueType(),
				price.price()
			))
			.toList();

		ticketPriceRepository.saveAll(ticketPrices);
		return TicketPricingPolicyCreateResponse.from(policy, ticketPrices);
	}

	@Override
	@Transactional(readOnly = true)
	public TicketPricingPolicyResponse get(UUID teamId, UUID memberId) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);

		TicketPricingPolicyEntity policy = ticketPricingPolicyRepository
			.findLatestActivePolicy(teamId)
			.orElseThrow(() -> new CustomException(ErrorCode.TICKET_PRICING_POLICY_NOT_FOUND));

		List<TicketPriceEntity> prices = ticketPriceRepository.findAllByPolicyId(policy.getId());

		return TicketPricingPolicyResponse.from(policy, prices);
	}

	private Map<UUID, SeatGradeEntity> getGrades(List<TicketPriceCreateCommand> ticketPriceRequest) {
		List<UUID> gradeIds = ticketPriceRequest.stream()
			.map(TicketPriceCreateCommand::gradeId)
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

	private void validateDuplicateTicketPrice(
		List<TicketPriceCreateCommand> ticketPriceRequest
	) {
		Set<TicketPriceCondition> uniqueConditions = new HashSet<>();

		for (TicketPriceCreateCommand price : ticketPriceRequest) {
			Preconditions.validate(
				uniqueConditions.add(
					new TicketPriceCondition(
						price.gradeId(),
						price.dayType(),
						price.leagueType()
					)
				),
				ErrorCode.TICKET_PRICE_CONDITION_ALREADY_EXISTS
			);
		}
	}

	private record TicketPriceCondition(
		UUID gradeId,
		TicketPricingDayType dayType,
		LeagueType leagueType
	) {
	}
}
