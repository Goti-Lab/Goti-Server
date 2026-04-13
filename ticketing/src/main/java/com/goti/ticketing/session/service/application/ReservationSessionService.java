package com.goti.ticketing.session.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.goti.ticketing.seat.handler.GameSeatUpdateHandler;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.ticketing.seat.service.domain.SeatHoldService;
import com.goti.ticketing.seat.service.domain.SeatStatusService;
import com.goti.ticketing.session.model.ReservationSessionCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationSessionService {
	private static final String KEY_DELIMITER = ":";

	private final RedisCache redisCache;
	private final SeatHoldService seatHoldService;
	private final SeatStatusService seatStatusService;
	private final GameSeatUpdateHandler gameSeatUpdateHandler;

	public ReservationSessionCache getOrCreate(
		UUID memberId,
		UUID gameId,
		boolean forceNewSession
	) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);
		Preconditions.validate(
			gameId != null,
			ErrorCode.BAD_REQUEST
		);

		ReservationSessionCache reservationSession = findReservationSession(
			memberId, gameId
		).orElseThrow(
			() -> new CustomException(ErrorCode.RESERVATION_SESSION_EXPIRED)
		);

		boolean isExpired = reservationSession.expiresAt().isBefore(LocalDateTime.now());

		if (forceNewSession || isExpired) {
			releaseHeldSeats(memberId, gameId);
			delete(memberId, gameId);
			if (isExpired)
				throw new CustomException(ErrorCode.RESERVATION_SESSION_EXPIRED);
			return create(memberId, gameId);
		}

		return reservationSession;
	}

	@Transactional
	public void validateActiveSession(UUID memberId, UUID gameId) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);
		Preconditions.validate(
			gameId != null,
			ErrorCode.BAD_REQUEST
		);

		ReservationSessionCache reservationSession = findReservationSession(
			memberId, gameId
		).orElseThrow(
			() -> new CustomException(ErrorCode.RESERVATION_SESSION_EXPIRED)
		);

		boolean isExpired = reservationSession.expiresAt().isBefore(LocalDateTime.now());

		if (isExpired) {
			releaseHeldSeats(memberId, gameId);
			delete(memberId, gameId);
			throw new CustomException(ErrorCode.RESERVATION_SESSION_EXPIRED);
		}
	}

	private ReservationSessionCache create(UUID memberId, UUID gameId) {
		ReservationSessionCache reservationSession = new ReservationSessionCache(
			UUID.randomUUID(),
			LocalDateTime.now().plus(RedisKey.RESERVATION_SESSION.getTtl())
		);
		redisCache.set(
			RedisKey.RESERVATION_SESSION,
			generateKeyParam(memberId, gameId),
			reservationSession
		);
		return reservationSession;
	}

	private Optional<ReservationSessionCache> findReservationSession(UUID memberId, UUID gameId) {
		return Optional.ofNullable(
			redisCache.get(
				RedisKey.RESERVATION_SESSION.getKey(generateKeyParam(memberId, gameId)),
				ReservationSessionCache.class
			)
		);
	}

	private boolean delete(UUID memberId, UUID gameId) {
		return redisCache.delete(
			RedisKey.RESERVATION_SESSION.getKey(generateKeyParam(memberId, gameId))
		);
	}

	private void releaseHeldSeats(UUID memberId, UUID gameId) {
		List<SeatHoldEntity> seatHolds = seatHoldService.getHoldingSeats(gameId, memberId);
		if (seatHolds.isEmpty()) {
			return;
		}

		List<UUID> seatIds = seatHolds.stream()
			.map(seatHold -> seatHold.getSeat().getId())
			.toList();

		Map<UUID, SeatStatusEntity> seatStatuses = seatStatusService.getByGameIdAndSeatIds(gameId, seatIds);

		for (SeatHoldEntity seatHold : seatHolds) {
			UUID seatId = seatHold.getSeat().getId();
			SeatStatusEntity seatStatus = seatStatuses.get(seatId);
			if (seatStatus != null) {
				seatStatus.release();
				gameSeatUpdateHandler.onSeatIncrease(gameId, 1);
			}
			seatHold.release();
		}
	}

	private String generateKeyParam(UUID memberId, UUID gameId) {
		return memberId + KEY_DELIMITER + gameId;
	}
}
