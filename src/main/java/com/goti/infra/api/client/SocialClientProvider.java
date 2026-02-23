package com.goti.infra.api.client;

import com.goti.constants.ProviderType;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SocialClientProvider {
	private final Map<ProviderType, SocialApiClient> clients;

	public SocialClientProvider(List<SocialApiClient> clientList) {
		this.clients = clientList.stream()
			.collect(Collectors.toUnmodifiableMap(
				SocialApiClient::getProviderType,
				Function.identity()
			));
	}

	public SocialApiClient getClient(ProviderType providerType) {
		return Optional.ofNullable(clients.get(providerType))
			.orElseThrow(
				() -> new CustomException(
					ErrorCode.INVALID_PROVIDER_TYPE, providerType.name()
				)
			);
	}
}
