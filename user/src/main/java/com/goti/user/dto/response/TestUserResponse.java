package com.goti.user.dto.response;

import java.util.UUID;

public record TestUserResponse(
	UUID userId,
	String mobile,
	String name,
	String accessToken
) {
}
