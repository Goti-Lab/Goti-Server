package com.goti.infra.api.client;

import com.goti.constants.ProviderType;

public interface SocialApiClient {
	ProviderType getProviderType();

	String getAccessToken(String code, String state);

	String getProviderId(String accessToken);
}
