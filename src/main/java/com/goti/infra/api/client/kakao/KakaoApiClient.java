package com.goti.infra.api.client.kakao;

import com.goti.config.properties.oauth.KakaoOauthProperties;
import com.goti.config.properties.oauth.NaverOauthProperties;
import com.goti.constants.ProviderType;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.infra.api.client.SocialApiClient;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoApiClient extends BaseRestClient implements SocialApiClient {

	private final KakaoOauthProperties properties;

	public KakaoApiClient(RestClient restClient, KakaoOauthProperties properties) {
		super(restClient.mutate());
		this.properties = properties;
	}

	@Override
	public ProviderType getProviderType() {
		return ProviderType.KAKAO;
	}

	// todo : accessToken API 구현 2026.02.23 오후 내로 완료 예정
	@Override
	public String getAccessToken(String code, String state) {
		return "";
	}

	// todo : getProviderId API 구현 2026.02.23 오후 내로 완료 예정
	@Override
	public String getProviderId(String accessToken) {
		return "";
	}
}
