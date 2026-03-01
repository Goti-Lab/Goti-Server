package com.goti.infra.api.dto.response.common;

import com.goti.constants.Gender;

import java.util.Map;

public record SocialUserInfoResponse(
	String providerId,
	String name,
	String email,
	String mobile,
	String birthDate,
	Gender gender
) {

	// todo : 프론트 개발자들과 협의 후 삭제 및 수정 예정 (fromNaver, fromKakao, fromGoogle)

	public static SocialUserInfoResponse fromNaver(Map<String, Object> response) {
		var resNode = (Map<String, Object>) response.get("response");
		return new SocialUserInfoResponse(
			getString(resNode, "id"),
			getString(resNode, "name"),
			getString(resNode, "email"),
			getString(resNode, "mobile"),
			formatDate(getString(resNode, "birthyear"), getString(resNode, "birthday")),
			Gender.fromString(getString(resNode, "gender"))
		);
	}

	public static SocialUserInfoResponse fromKakao(Map<String, Object> response) {
		var account = (Map<String, Object>) response.get("kakao_account");
		var profile = (Map<String, Object>) account.get("profile");
		return new SocialUserInfoResponse(
			getString(response, "id"), // 카카오는 최상위 id가 숫자임
			getString(profile, "nickname"),
			getString(account, "email"),
			getString(account, "phone_number"),
			formatDate(getString(account, "birthyear"), getString(account, "birthday")),
			Gender.fromString(getString(account, "gender"))
		);
	}

	public static SocialUserInfoResponse fromGoogle(Map<String, Object> response) {
		return new SocialUserInfoResponse(
			getString(response, "sub"),
			getString(response, "name"),
			getString(response, "email"),
			getString(response, "mobile"),
			getString(response, "birthday"),
			Gender.fromString(getString(response, "gender"))
		);
	}

	private static String formatDate(String year, String day) {
		if (year == null || day == null) return null;
		return year + "-" + day.replace("-", "");
	}

	private static String getString(Map<String, Object> map, String key) {
		Object value = map.get(key);
		return (value != null) ? String.valueOf(value) : null;
	}
}
