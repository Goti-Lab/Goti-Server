package com.goti.ticketing.domain.entity.seat;

import com.goti.domain.base.CreationTimestampEntity;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static lombok.AccessLevel.*;

@Getter
@Entity
@Table(name = "seat_sections")
@NoArgsConstructor(access = PROTECTED)
public class SeatSectionEntity extends ModificationTimestampEntity {

	@JoinColumn(name = "grade_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private SeatGradeEntity seatGrade;

	@Column(nullable = false)
	private UUID stadiumId;

	@Column(nullable = false)
	private String sectionCode;

	@Column(nullable = false)
	private Integer capacity;

	private SeatSectionEntity(
		SeatGradeEntity seatGrade,
		UUID stadiumId,
		String sectionCode,
		Integer capacity
	) {
		this.seatGrade = seatGrade;
		this.stadiumId = stadiumId;
		this.sectionCode = sectionCode;
		this.capacity = capacity;
	}

	public static SeatSectionEntity create(
		SeatGradeEntity seatGrade,
		UUID stadiumId,
		String sectionCode,
		Integer capacity
	) {
		validate(stadiumId, sectionCode, capacity);
		return new SeatSectionEntity(seatGrade, stadiumId, sectionCode, capacity);
	}

	private static void validate(
		UUID stadiumId,
		String sectionCode,
		Integer capacity
	) {
		Preconditions.domainValidate(
			stadiumId != null,
			"구장 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(sectionCode),
			"구역 코드는 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			capacity != null && capacity > 0,
			"수용 인원은 0보다 커야 합니다."
		);
	}
}
