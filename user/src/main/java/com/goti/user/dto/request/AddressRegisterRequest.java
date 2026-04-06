package com.goti.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "주소 등록 요청")
public record AddressRegisterRequest(

	@Schema(description = "우편번호", example = "09123")
	@NotBlank(message = "우편번호는 필수 항목입니다.")
	@Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
	@Pattern(regexp = "\\d+", message = "우편번호는 숫자만 입력할 수 있습니다.")
	String zipCode,

	@Schema(description = "기본주소", example = "서울특별시 강남구 학동로 343")
	@NotBlank(message = "기본 주소는 필수 항목입니다.")
	String baseAddress,

	@Schema(description = "상세주소", example = "(논현동, 포바강남타워) 4층, 15층")
	@NotBlank(message = "상세 주소는 필수 항목입니다.")
	String detailAddress
) {
}
