package com.goti.infra.api.client;

import com.goti.constants.OAuthProvider;
import com.goti.infra.api.dto.response.common.SocialUserInfoResponse;

public interface SocialApiClient {
	OAuthProvider getProviderType();

	String getAccessToken(String authCode, String state);

	SocialUserInfoResponse getSocialUserInfo(String accessToken);
}
