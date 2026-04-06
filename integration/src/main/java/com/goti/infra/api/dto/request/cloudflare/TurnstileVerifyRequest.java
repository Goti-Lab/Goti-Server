package com.goti.infra.api.dto.request.cloudflare;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public record TurnstileVerifyRequest(
	String secret,
	String response
) {
	public MultiValueMap<String, String> toFormData() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("secret", secret);
		formData.add("response", response);
		return formData;
	}
}
