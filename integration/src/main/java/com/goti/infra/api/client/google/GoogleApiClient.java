package com.goti.infra.api.client.google;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.goti.constants.OAuthProvider;

import com.goti.infra.api.dto.response.common.SocialUserInfoResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.oauth.GoogleOauthProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.dto.request.google.GoogleTokenRequest;
import com.goti.infra.api.dto.response.common.SocialAccessTokenResponse;

@Component
public class GoogleApiClient extends BaseRestClient implements SocialApiClient {

	private final GoogleOauthProperties properties;

	public GoogleApiClient(RestClient.Builder builder, GoogleOauthProperties properties) {
		super(builder);
		this.properties = properties;
	}

	@Override
	public OAuthProvider getProviderType() {
		return OAuthProvider.GOOGLE;
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

	@Override
	public SocialUserInfoResponse getSocialUserInfo(String accessToken) {
		Map<String, Object> response = get(
			properties.userInfoUrl(),
			createBearerHeader(accessToken),
			null,
			Map.class
		);
		return SocialUserInfoResponse.fromGoogle(response);
	}
}
