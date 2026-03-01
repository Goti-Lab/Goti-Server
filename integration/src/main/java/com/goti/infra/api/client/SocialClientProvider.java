package com.goti.infra.api.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.goti.constants.OAuthProvider;

import org.springframework.stereotype.Component;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;

@Component
public class SocialClientProvider {
	private final Map<OAuthProvider, SocialApiClient> clients;

	public SocialClientProvider(List<SocialApiClient> clientList) {
		this.clients = clientList.stream()
			.collect(Collectors.toUnmodifiableMap(
				SocialApiClient::getProviderType,
				Function.identity()
			));
	}

	public SocialApiClient getClient(OAuthProvider providerType) {
		return Optional.ofNullable(clients.get(providerType))
			.orElseThrow(
				() -> new CustomException(
					ErrorCode.INVALID_PROVIDER_TYPE, providerType.name()
				)
			);
	}
}
