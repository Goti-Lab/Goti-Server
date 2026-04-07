package com.goti.infra.api.dto.response.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SocialAccessTokenResponse(
	@JsonProperty("access_token")
	String accessToken
) {
}
