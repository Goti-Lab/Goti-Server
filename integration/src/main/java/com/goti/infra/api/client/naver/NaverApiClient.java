package com.goti.infra.api.client.naver;

import com.goti.constants.OAuthProvider;

import com.goti.infra.api.dto.response.common.SocialUserInfoResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.oauth.NaverOauthProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.dto.request.naver.NaverTokenRequest;
import com.goti.infra.api.dto.response.common.SocialAccessTokenResponse;

import java.util.Map;

@Component
public class NaverApiClient extends BaseRestClient implements SocialApiClient {

	private final NaverOauthProperties properties;

	public NaverApiClient(RestClient restClient, NaverOauthProperties properties) {
		super(restClient.mutate());
		this.properties = properties;
	}

	@Override
	public OAuthProvider getProviderType() {
		return OAuthProvider.NAVER;
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

	@Override
	public SocialUserInfoResponse getSocialUserInfo(String accessToken) {
		Map<String, Object> response = get(
			properties.userInfoUrl(),
			createBearerHeader(accessToken),
			null,
			Map.class
		);
		return SocialUserInfoResponse.fromNaver(response);
	}


}
