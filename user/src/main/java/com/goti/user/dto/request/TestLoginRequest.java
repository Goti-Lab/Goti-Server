package com.goti.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TestLoginRequest(

	@NotBlank(message = "휴대전화번호는 필수 항목입니다.")
	@Pattern(regexp = "^000\\d{8}$", message = "테스트 유저 mobile은 000XXXXXXXX 형식이어야 합니다")
	String mobile
) {
}
