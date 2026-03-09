package com.goti.seat.service.domain;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.seat.SeatEntity;
import com.goti.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.seat.dto.response.BulkCreateSeatsResponse;
import com.goti.seat.dto.response.SeatResponse;
import com.goti.seat.repository.SeatRepository;
import com.goti.seat.repository.SeatSectionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {
	private static final int MAX_BULK_CREATE_SEAT_COUNT = 1000;

	private final SeatSectionRepository seatSectionRepository;
	private final SeatRepository seatRepository;

	@Override
	@Transactional
	public BulkCreateSeatsResponse create(
		UUID sectionId,
		String rowName,
		Integer startSeatNumber,
		Integer endSeatNumber
	) {
		Preconditions.validate(
			startSeatNumber <= endSeatNumber,
			ErrorCode.INVALID_SEAT_NUMBER_RANGE
		);
		int rangeSize = endSeatNumber - startSeatNumber + 1;

		Preconditions.validate(
			rangeSize <= MAX_BULK_CREATE_SEAT_COUNT,
			ErrorCode.SEAT_BULK_CREATE_LIMIT_EXCEEDED
		);

		SeatSectionEntity seatSection = seatSectionRepository.findById(sectionId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_SECTION_NOT_FOUND));

		List<Integer> seatNumbers = IntStream.rangeClosed(startSeatNumber, endSeatNumber)
			.boxed()
			.toList();

		long currentSeatCount = seatRepository.countBySeatSection_Id(sectionId);
		long totalSeatCount = currentSeatCount + seatNumbers.size();

		Preconditions.validate(
			totalSeatCount <= seatSection.getCapacity(),
			ErrorCode.SEAT_SECTION_CAPACITY_EXCEEDED
		);

		List<SeatEntity> existingSeats = seatRepository.findAllInRow(
			sectionId,
			rowName,
			seatNumbers
		);

		Preconditions.validate(
			existingSeats.isEmpty(),
			ErrorCode.SEAT_ALREADY_EXISTS
		);

		List<SeatEntity> seats = seatNumbers.stream()
			.map(seatNum -> SeatEntity.create(seatSection, rowName, seatNum))
			.toList();

		seatRepository.saveAll(seats);
		return BulkCreateSeatsResponse.from(
			sectionId,
			rowName,
			startSeatNumber,
			endSeatNumber
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SeatResponse> get(UUID sectionId) {
		return seatRepository.findAllBySection(sectionId).stream()
			.map(SeatResponse::from)
			.toList();
	}
}
