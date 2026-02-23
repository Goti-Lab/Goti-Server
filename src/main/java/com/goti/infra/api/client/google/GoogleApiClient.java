package com.goti.infra.api.client.google;

import com.goti.config.properties.oauth.GoogleOauthProperties;
import com.goti.constants.ProviderType;
import com.goti.infra.api.base.BaseRestClient;

import com.goti.infra.api.client.SocialApiClient;

import com.goti.infra.api.dto.request.google.GoogleTokenRequest;

import com.goti.infra.api.dto.response.common.SocialAccessTokenResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleApiClient extends BaseRestClient implements SocialApiClient {

	private final GoogleOauthProperties properties;

	public GoogleApiClient(RestClient restClient, GoogleOauthProperties properties) {
		super(restClient.mutate());
		this.properties = properties;
	}

	@Override
	public ProviderType getProviderType() {
		return ProviderType.GOOGLE;
	}

	@Override
	public String getAccessToken(String code, String state) {
		String originCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
		GoogleTokenRequest request = new GoogleTokenRequest(
			properties.clientId(),
			properties.clientSecret(),
			properties.redirectUri(),
			originCode
		);

		SocialAccessTokenResponse response = post(
			properties.tokenUrl(),
			request.toFormData(),
			MediaType.APPLICATION_FORM_URLENCODED,
			SocialAccessTokenResponse.class
		);

		return response.accessToken();
	}

	// todo : getProviderId API 구현 2026.02.23 오후 내로 완료 예정
	@Override
	public String getProviderId(String accessToken) {
		return "";
	}
}
