package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OAuthProvider {
	GOOGLE("google"),
	KAKAO("kakao"),
	NAVER("naver");

	private final String value;
}
