package com.goti.infra.api.client.naver;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.oauth.NaverOauthProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.dto.request.naver.NaverTokenRequest;
import com.goti.infra.api.dto.response.common.SocialAccessTokenResponse;
import com.goti.infra.constants.ProviderType;

@Component
public class NaverApiClient extends BaseRestClient implements SocialApiClient {

	private final NaverOauthProperties properties;

	public NaverApiClient(RestClient restClient, NaverOauthProperties properties) {
		super(restClient.mutate());
		this.properties = properties;
	}

	@Override
	public ProviderType getProviderType() {
		return ProviderType.NAVER;
	}

	@Override
	public String getAccessToken(String code, String state) {
		NaverTokenRequest request = new NaverTokenRequest(
			properties.clientId(),
			properties.clientSecret(),
			properties.redirectUri(),
			code,
			state
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
