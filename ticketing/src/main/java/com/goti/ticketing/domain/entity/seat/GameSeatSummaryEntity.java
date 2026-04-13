package com.goti.ticketing.domain.entity.seat;

import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "game_seat_summaries")
@NoArgsConstructor(access = PROTECTED)
public class GameSeatSummaryEntity extends ModificationTimestampEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_schedule_id")
	private GameScheduleEntity gameSchedule;

	@Column(nullable = false)
	private Integer availableCount;

	@Column(nullable = false)
	private Integer totalCount;

	private GameSeatSummaryEntity(GameScheduleEntity gameSchedule, Integer totalCount) {
		this.gameSchedule = gameSchedule;
		this.availableCount = totalCount;
		this.totalCount = totalCount;
	}

	public static GameSeatSummaryEntity create(GameScheduleEntity gameSchedule, Integer totalCount) {
		return new GameSeatSummaryEntity(gameSchedule, totalCount);
	}
}
