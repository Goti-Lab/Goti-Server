package com.goti.dto.request;

import com.goti.config.validator.PastYear;
import com.goti.constants.TeamCode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BaseballTeamCreateRequest(

	@Schema(description = "구단코드", example = "SS")
	@NotNull(message = "구단(팀) 코드는 필수 항목입니다.")
	TeamCode teamCode,

	@Schema(description = "구단명", example = "삼성라이온즈")
	@NotBlank(message = "구단(팀)이름은 필수 항목입니다.")
	String teamName,

	@Schema(description = "구단명(영문)", example = "Samsung Lions")
	@NotBlank(message = "구단(팀)이름은(영문) 필수 항목입니다.")
	String teamNameEn,

	@Schema(description = "스폰서", example = "삼성")
	@NotBlank(message = "구단(팀)의 스폰서는 필수 항목입니다.")
	String sponsor,

	@Schema(description = "연고지", example = "대구")
	@NotBlank(message = "구단(팀) 연고지는 필수 항목입니다.")
	String homeGround,

	@Schema(description = "창단년도", example = "1995")
	@PastYear(yearName = "창단년도")
	@NotNull(message = "구단(팀) 창단년도는 필수 항목입니다.")
	Integer foundedYear,

	@Schema(description = "도로명주소", example = "대구광역시 수성구 야구전설로 1")
	@NotBlank(message = "구단(팀) 사무실 도로명 주소는 필수 항목입니다.")
	String officeAddress,

	@Schema(description = "구단 사무실 우편주소", example = "42250")
	@NotBlank(message = "구단(팀) 사무실 우편주소는 필수 항목입니다.")
	String zipCode,

	@Schema(description = "구단 사이트 주소", example = "https://www.samsunglions.com/")
	@NotBlank(message = "구단(팀) 사이트 주소는 필수 항목입니다.")
	String siteAddress,

	@Schema(description = "구단주", example = "홍길동")
	@NotBlank(message = "구단주명은 필수 항목입니다.")
	String owner,

	@Schema(description = "구단주 대행", example = "홍길동")
	String ownerAgency,

	@Schema(description = "대표이사", example = "홍길동")
	String ceo,

	@Schema(description = "단장", example = "홍길동")
	@NotBlank(message = "구단(팀)의 단장명은 필수 항목입니다.")
	String generalManager,

	@Schema(description = "감독", example = "홍길동")
	@NotBlank(message = "구단(팀)의 감독명은 필수 항목입니다.")
	String director,


	@Schema(
		description = "구단 로고 이미지 URL",
		example = "https://image.goti.com/logos/samsung_lions.png"
	)
	String logoUrl
) {
}
