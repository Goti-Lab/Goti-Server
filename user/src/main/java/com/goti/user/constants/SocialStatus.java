package com.goti.user.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialStatus {
	ACTIVE("활성화"),
	INACTIVE("비활성화");

	private final String description;
}
