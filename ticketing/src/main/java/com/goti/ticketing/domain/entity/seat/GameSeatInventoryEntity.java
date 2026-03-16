package com.goti.ticketing.domain.entity.seat;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static lombok.AccessLevel.*;

@Getter
@Entity
@Table(
	name = "game_seat_inventories",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_game_grade", columnNames = {"game_id", "seat_grade_id"})
	},
	indexes = {
		@Index(name = "idx_game", columnList = "game_id")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class GameSeatInventoryEntity extends ModificationTimestampEntity {

	@Column(nullable = false)
	private UUID gameId;

	@Column(nullable = false)
	private UUID seatGradeId;

	@Column(nullable = false)
	private Integer totalCount;

	@Column(nullable = false)
	private Integer availableCount;

	@Column(nullable = false)
	private Integer heldCount;

	@Column(nullable = false)
	private Integer soldCount;

	@Column(nullable = false)
	private Integer blockedCount;

	private GameSeatInventoryEntity(
		UUID gameId,
		UUID seatGradeId,
		Integer totalCount,
		Integer availableCount
	) {
		this.gameId = gameId;
		this.seatGradeId = seatGradeId;
		this.totalCount = totalCount;
		this.availableCount = availableCount;
		this.heldCount = 0;
		this.soldCount = 0;
		this.blockedCount = 0;
	}

	public static GameSeatInventoryEntity create(
		UUID gameId,
		UUID seatGradeId,
		Integer totalCount,
		Integer availableCount
	) {
		validate(gameId, seatGradeId, totalCount, availableCount);
		return new GameSeatInventoryEntity(gameId, seatGradeId, totalCount, availableCount);
	}

	private static void validate(
		UUID gameId,
		UUID seatGradeId,
		Integer totalCount,
		Integer availableCount
	) {
		Preconditions.domainValidate(
			gameId != null,
			"경기 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			seatGradeId != null,
			"좌석 등급 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			totalCount != null && totalCount >= 0,
			"전체 좌석 수는 0 이상이어야 합니다."
		);
		Preconditions.domainValidate(
			availableCount != null && availableCount >= 0,
			"잔여 좌석 수는 0 이상이어야 합니다."
		);
		Preconditions.domainValidate(
			availableCount <= totalCount,
			"잔여 좌석 수는 전체 좌석 수보다 클 수 없습니다."
		);
	}
}
