package com.goti.infra.api.client.kakao;

import com.goti.constants.OAuthProvider;

import com.goti.infra.api.dto.response.common.SocialUserInfoResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.oauth.KakaoOauthProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.dto.request.kakao.KakaoTokenRequest;
import com.goti.infra.api.dto.response.common.SocialAccessTokenResponse;

import java.util.Map;

@Component
public class KakaoApiClient extends BaseRestClient implements SocialApiClient {

	private final KakaoOauthProperties properties;

	public KakaoApiClient(RestClient.Builder builder, KakaoOauthProperties properties) {
		super(builder);
		this.properties = properties;
	}

	@Override
	public OAuthProvider getProviderType() {
		return OAuthProvider.KAKAO;
	}

	@Override
	public String getAccessToken(String code, String state) {
		KakaoTokenRequest request = new KakaoTokenRequest(
			properties.clientId(),
			properties.clientSecret(),
			properties.redirectUri(),
			code
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
		return SocialUserInfoResponse.fromKakao(response);
	}
}
