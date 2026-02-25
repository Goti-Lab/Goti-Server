package com.goti.infra.api.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.constants.ProviderType;

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
