package com.goti.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.domain.base.BaseUuidEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_price_histories")
@NoArgsConstructor(access = PROTECTED)
public class ResalePriceHistoryEntity extends BaseUuidEntity {

	@Column(nullable = false)
	private UUID gameId;

	@Column(nullable = false)
	private UUID seatId;

	@Column(nullable = false)
	private UUID gradeId;

	@Column(nullable = false)
	private Integer transactionPrice;

	@Column(nullable = false)
	private LocalDate transactionDate;

	@Column(nullable = false)
	private LocalDateTime transactionTime;

	private ResalePriceHistoryEntity(
		UUID gameId,
		UUID seatId,
		UUID gradeId,
		Integer transactionPrice,
		LocalDate transactionDate,
		LocalDateTime transactionTime
	) {
		this.gameId = gameId;
		this.seatId = seatId;
		this.gradeId = gradeId;
		this.transactionPrice = transactionPrice;
		this.transactionDate = transactionDate;
		this.transactionTime = transactionTime;
	}

	public static ResalePriceHistoryEntity create(
		UUID gameId,
		UUID seatId,
		UUID gradeId,
		Integer transactionPrice,
		LocalDate transactionDate,
		LocalDateTime transactionTime
	) {
		validate(gameId, seatId, gradeId, transactionPrice, transactionDate, transactionTime);

		return new ResalePriceHistoryEntity(
			gameId,
			seatId,
			gradeId,
			transactionPrice,
			transactionDate,
			transactionTime
		);
	}

	private static void validate(
		UUID gameId,
		UUID seatId,
		UUID gradeId,
		Integer transactionPrice,
		LocalDate transactionDate,
		LocalDateTime transactionTime
	) {
		Preconditions.domainValidate(gameId != null, "게임 ID는 비어 있을 수 없습니다");
		Preconditions.domainValidate(seatId != null, "좌석 ID는 비어 있을 수 없습니다");
		Preconditions.domainValidate(gradeId != null, "등급 ID는 비어 있을 수 없습니다");
		Preconditions.domainValidate(transactionPrice != null && transactionPrice >= 0, "거래 가격은 0 이상이어야 합니다");
		Preconditions.domainValidate(transactionDate != null, "체결 날짜는 비어 있을 수 없습니다");
		Preconditions.domainValidate(transactionTime != null, "체결 일시는 비어 있을 수 없습니다");

	}

}
