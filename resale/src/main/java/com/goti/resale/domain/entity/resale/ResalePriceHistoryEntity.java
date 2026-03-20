package com.goti.resale.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.math.BigDecimal;
import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_price_histories",
	indexes = {
		@Index(name = "idx_game_id", columnList = "game_id"),
		@Index(name = "idx_seat_id", columnList = "seat_id"),
		@Index(name = "idx_section_id", columnList = "section_id"),
		@Index(name = "idx_grade_id", columnList = "grade_id"),
		@Index(name = "idx_resale_lookup", columnList = "game_id, grade_id, created_at")
	})
@NoArgsConstructor(access = PROTECTED)
public class ResalePriceHistoryEntity extends ModificationTimestampEntity {

	@Column(nullable = false)
	private UUID gameId;

	@Column(nullable = false)
	private UUID seatId;

	@Column(nullable = false)
	private UUID sectionId;

	@Column(nullable = false)
	private UUID gradeId;

	@Column(nullable = false)
	private Integer transactionPrice;

	private BigDecimal changePercent;

	private ResalePriceHistoryEntity(
		UUID gameId,
		UUID seatId,
		UUID sectionId,
		UUID gradeId,
		Integer transactionPrice,
		BigDecimal changePercent
	) {
		this.gameId = gameId;
		this.seatId = seatId;
		this.sectionId = sectionId;
		this.gradeId = gradeId;
		this.transactionPrice = transactionPrice;
		this.changePercent = changePercent;
	}

	public static ResalePriceHistoryEntity create(
		UUID gameId,
		UUID seatId,
		UUID sectionId,
		UUID gradeId,
		Integer transactionPrice,
		BigDecimal changePercent
	) {
		validate(gameId, seatId, sectionId, gradeId, transactionPrice);

		return new ResalePriceHistoryEntity(
			gameId,
			seatId,
			sectionId,
			gradeId,
			transactionPrice,
			changePercent
		);
	}

	private static void validate(
		UUID gameId,
		UUID seatId,
		UUID sectionId,
		UUID gradeId,
		Integer transactionPrice
	) {
		Preconditions.domainValidate(gameId != null, "게임 ID는 비어 있을 수 없습니다");
		Preconditions.domainValidate(seatId != null, "좌석 ID는 비어 있을 수 없습니다");
		Preconditions.domainValidate(sectionId != null, "구역 ID는 비어 있을 수 없습니다");
		Preconditions.domainValidate(gradeId != null, "등급 ID는 비어 있을 수 없습니다");
		Preconditions.domainValidate(transactionPrice != null && transactionPrice >= 0, "거래 가격은 0 이상이어야 합니다");
	}

}
