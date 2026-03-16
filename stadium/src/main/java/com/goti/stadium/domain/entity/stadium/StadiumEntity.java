package com.goti.stadium.domain.entity.stadium;

import static lombok.AccessLevel.*;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.StringUtils;

import com.goti.global.validation.Preconditions;
import com.goti.domain.base.ModificationTimestampEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stadiums")
@NoArgsConstructor(access = PROTECTED)
public class StadiumEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private String stadiumName;

	@Column(nullable = false)
	private String location;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String district;

	@Column(nullable = false)
	private String roadAddress;

	@Column(nullable = false, precision = 10, scale = 8)
	private BigDecimal latitude;

	@Column(nullable = false, precision = 11, scale = 8)
	private BigDecimal longitude;

	@Column(nullable = false)
	private Integer totalSeats;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> seatMapConfig;

	private StadiumEntity(
		String stadiumName,
		String location,
		String city,
		String district,
		String roadAddress,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer totalSeats,
		Map<String, Object> seatMapConfig
	) {
		this.stadiumName = stadiumName;
		this.location = location;
		this.city = city;
		this.district = district;
		this.roadAddress = roadAddress;
		this.latitude = latitude;
		this.longitude = longitude;
		this.totalSeats = totalSeats;
		this.seatMapConfig = seatMapConfig;
	}

	public static StadiumEntity create(
		String stadiumName,
		String location,
		String city,
		String district,
		String roadAddress,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer totalSeats,
		Map<String, Object> seatMapConfig
	) {

		validate(
			stadiumName,
			location,
			city,
			district,
			roadAddress,
			latitude,
			longitude,
			totalSeats
		);

		return new StadiumEntity(
			stadiumName,
			location,
			city,
			district,
			roadAddress,
			latitude,
			longitude,
			totalSeats,
			seatMapConfig
		);
	}

	private static void validate(
		String stadiumName,
		String location,
		String city,
		String district,
		String roadAddress,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer totalSeats
	) {
		Preconditions.domainValidate(
			StringUtils.hasText(stadiumName), "구장명은 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(location), "지역명은 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(city), "시/도는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(district), "시/군/구는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(roadAddress), "도로명 주소는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			latitude != null
				&& latitude.compareTo(BigDecimal.valueOf(-90)) >= 0
				&& latitude.compareTo(BigDecimal.valueOf(90)) <= 0,
			"위도는 -90에서 90 사이의 값이어야 합니다."
		);

		Preconditions.domainValidate(
			longitude != null &&
				longitude.compareTo(BigDecimal.valueOf(-180)) >= 0
				&& longitude.compareTo(BigDecimal.valueOf(180)) <= 0,
			"경도는 -180에서 180 사이의 값이어야 합니다."
		);

		Preconditions.domainValidate(
			totalSeats != null && totalSeats > 0,
			"총 좌석 수는 0보다 커야 합니다."
		);
	}
}
