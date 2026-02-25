package com.goti.infra.api.dto.request.google;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public record GoogleTokenRequest(
	String clientId,
	String clientSecret,
	String redirectUri,
	String code
) {
	private static final String GRANT_TYPE = "authorization_code";

	public MultiValueMap<String, String> toFormData() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", GRANT_TYPE);
		formData.add("client_id", clientId);

		if (clientSecret != null && !clientSecret.isBlank()) {
			formData.add("client_secret", clientSecret);
		}
		formData.add("redirect_uri", redirectUri);
		formData.add("code", code);

		return formData;
	}
}
