package com.goti.ticketing.domain.entity.seat;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.springframework.util.StringUtils;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Getter
@Entity
@Table(name = "seat_grades",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_stadium_seat_grade_name", columnNames = {"stadium_id", "name"})
	})
@NoArgsConstructor(access = PROTECTED)
public class SeatGradeEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID stadiumId;

	@Column(nullable = false)
	private String name;

	@Column
	private String displayColorHex;

	private SeatGradeEntity(
		UUID stadiumId,
		String name,
		String displayColorHex
	) {
		this.stadiumId = stadiumId;
		this.name = name;
		this.displayColorHex = displayColorHex;
	}

	public static SeatGradeEntity create(
		UUID stadiumId,
		String name,
		String displayColorHex
	) {
		validate(stadiumId, name);
		return new SeatGradeEntity(stadiumId, name, displayColorHex);
	}

	private static void validate(
		UUID stadiumId,
		String name
	) {
		Preconditions.domainValidate(
			stadiumId != null,
			"구장 ID는 필수입니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(name),
			"좌석 등급명은 비어 있을 수 없습니다."
		);
	}
}
