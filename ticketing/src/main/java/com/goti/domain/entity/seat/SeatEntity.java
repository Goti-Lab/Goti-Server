package com.goti.domain.entity.seat;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import static lombok.AccessLevel.*;

@Getter
@Entity
@Table(
	name = "seats",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_section_row_seat_num",
			columnNames = {"section_id", "row_name", "seat_num"}
		)
	}
)
@NoArgsConstructor(access = PROTECTED)
public class SeatEntity extends ModificationTimestampEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id", nullable = false)
	private SeatSectionEntity seatSection;

	@Column(name = "row_name", nullable = false)
	private String rowName;

	@Column(name = "seat_num", nullable = false)
	private Integer seatNum;

	@Column(name = "is_available", nullable = false)
	private boolean available;

	private SeatEntity(
		SeatSectionEntity seatSection,
		String rowName,
		Integer seatNum
	) {
		this.seatSection = seatSection;
		this.rowName = rowName;
		this.seatNum = seatNum;
		this.available = true;
	}

	public static SeatEntity create(
		SeatSectionEntity seatSection,
		String rowName,
		Integer seatNum
	) {
		validate(rowName, seatNum);
		return new SeatEntity(seatSection, rowName, seatNum);
	}

	private static void validate(
		String rowName,
		Integer seatNum
	) {
		Preconditions.domainValidate(
			StringUtils.hasText(rowName),
			"행 번호는 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			seatNum != null && seatNum > 0,
			"좌석 번호는 0보다 커야 합니다."
		);
	}
}
