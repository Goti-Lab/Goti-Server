package com.goti.domain.entity.seat;

import static lombok.AccessLevel.*;

import com.goti.constants.SeatStatus;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.domain.entity.game.GameScheduleEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "seat_statuses",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_game_seat", columnNames = {"game_id", "seat_id"})
	},
	indexes = {
		@Index(name = "idx_game_status", columnList = "game_id, status")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class SeatStatusEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false)
	private GameScheduleEntity game;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private SeatEntity seat;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SeatStatus status;

	private SeatStatusEntity(
		GameScheduleEntity game,
		SeatEntity seat
	) {
		this.game = game;
		this.seat = seat;
		this.status = SeatStatus.AVAILABLE;
	}

	public static SeatStatusEntity create(
		GameScheduleEntity game,
		SeatEntity seat
	) {
		return new SeatStatusEntity(game, seat);
	}
}
