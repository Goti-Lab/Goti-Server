package com.goti.domain.entity.game;

import com.goti.constants.TicketingStatus;
import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "game_ticketing_statuses")
@NoArgsConstructor(access = PROTECTED)
public class GameTicketingStatusEntity extends ModificationTimestampEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_schedule_id", nullable = false)
	private GameScheduleEntity gameSchedule;

	@Column(nullable = false)
	private LocalDateTime ticketingOpenedAt;

	@Column(nullable = false)
	private LocalDateTime ticketingEndAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketingStatus status;

	private GameTicketingStatusEntity(
		GameScheduleEntity gameSchedule,
		LocalDateTime ticketingOpenedAt,
		LocalDateTime ticketingEndAt
	) {
		this.gameSchedule = gameSchedule;
		this.ticketingOpenedAt = ticketingOpenedAt;
		this.ticketingEndAt = ticketingEndAt;
		this.status = TicketingStatus.UPCOMING;
	}

	public static GameTicketingStatusEntity create(
		final GameScheduleEntity gameSchedule,
		final LocalDateTime ticketingOpenedAt,
		final LocalDateTime ticketingEndAt
	) {
		validate(gameSchedule, ticketingOpenedAt, ticketingEndAt);

		return new GameTicketingStatusEntity(
			gameSchedule, ticketingOpenedAt, ticketingEndAt
		);
	}

	private static void validate(
		GameScheduleEntity gameSchedule,
		LocalDateTime ticketingOpenedAt,
		LocalDateTime ticketingEndAt
	) {
		LocalDateTime now = LocalDateTime.now();
		Preconditions.domainValidate(
			gameSchedule != null,
			"게임일정 값은 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			ticketingOpenedAt != null && ticketingOpenedAt.isAfter(now),
			"예매 시작 시점은 현재보다 미래여야 합니다."
		);

		Preconditions.domainValidate(
			ticketingEndAt != null && ticketingEndAt.isAfter(now),
			"예매 종료 시점은 현재보다 미래여야 합니다."
		);

		if (ticketingOpenedAt != null && ticketingEndAt != null) {
			Preconditions.domainValidate(
				ticketingEndAt.isAfter(ticketingOpenedAt),
				"예매 종료 시점은 시작 시점보다 이후여야 합니다."
			);
		}
	}

}
