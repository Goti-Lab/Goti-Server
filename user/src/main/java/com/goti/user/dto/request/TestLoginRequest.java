package com.goti.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TestLoginRequest(
	@NotBlank String mobile
) {
}
