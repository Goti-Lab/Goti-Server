package com.goti.stadium.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Map;

public record StadiumCreateRequest(

	@Schema(description = "구장명", example = "대구삼성라이온즈파크")
	@NotBlank(message = "구장명은 필수 항목입니다.")
	String stadiumName,

	@Schema(description = "구장 지역명", example = "대구")
	@NotBlank(message = "구장 지역명은 필수 항목입니다.")
	String location,

	@Schema(description = "시/도", example = "대구광역시")
	@NotBlank(message = "시/도 값은 필수 항목입니다.")
	String city,

	@Schema(description = "시/군/구", example = "수성구")
	@NotBlank(message = "시/군/구 값은 필수 항목입니다.")
	String district,

	@Schema(description = "도로명주소", example = "대구광역시 수성구 야구전설로 1")
	@NotBlank(message = "도로명주소는 필수 항목입니다.")
	String roadAddress,

	@Schema(description = "위도", example = "35.8411")
	@NotNull(message = "구장의 위도 값은 필수 항목입니다.")
	@DecimalMin(value = "-90.0", message = "위도 값은 -90에서 90 사이여야 합니다")
	@DecimalMax(value = "90.0", message = "위도 값은 -90에서 90 사이여야 합니다")
	BigDecimal latitude,

	@Schema(description = "경도", example = "128.6811")
	@NotNull(message = "구장의 경도 값은 필수 항목입니다.")
	@DecimalMin(value = "-180.0", message = "경도 값은 -180에서 180 사이여야 합니다.")
	@DecimalMax(value = "180.0", message = "경도 값은 -180에서 180 사이여야 합니다.")
	BigDecimal longitude,

	@Schema(description = "총 좌석 수", example = "24000")
	@NotNull(message = "구장 좌석수는 필수 항목입니다.")
	@Positive(message = "구장 좌석수는 0보다 커야 합니다.")
	Integer totalSeats,

	@Schema(description = "좌석 배치 설정 정보", example = "{\"rows\": 10, \"cols\": 20}")
	@NotNull(message = "구장 좌석 배치 설정 정보는 필수 항목입니다.")
	Map<String, Object> seatMapConfig

) {
}