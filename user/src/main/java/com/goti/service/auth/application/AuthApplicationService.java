package com.goti.service.auth.application;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.client.SocialClientProvider;

import com.goti.infra.api.dto.response.common.SocialStateResponse;
import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.ProviderType;

import com.goti.infra.constants.redis.RedisKey;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {
	private final SocialClientProvider socialClientProvider;
	private final RedisCache redisCache;

	static final String KEY_SEPARATOR = ":";

	public void login(ProviderType provider, String code, String state) {
		validateState(provider, state);
		String accessToken = getAccessToken(provider, code, state);
		log.info("accessToken :: {}", accessToken);
	}


	public SocialStateResponse issueState(ProviderType provider) {
		if (provider == ProviderType.KAKAO) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
		String state = UUID.randomUUID().toString();
		String keyParam = provider + KEY_SEPARATOR + state;

		redisCache.set(RedisKey.OAUTH_STATE, keyParam, true);

		return SocialStateResponse.of(state);
	}

	private void validateState(ProviderType provider, String state) {
		if (provider == ProviderType.KAKAO) return;

		if (state == null || state.isBlank()) {
			throw new CustomException(ErrorCode.MISSING_PARAMETER, "state");
		}
		String keyParam = provider + KEY_SEPARATOR + state;
		String stateKey = RedisKey.OAUTH_STATE.getKey(keyParam);
		if (!redisCache.consume(stateKey)) {
			throw new CustomException(ErrorCode.INVALID_STATE);
		}
	}

	private String getAccessToken(ProviderType provider, String code, String state) {
		SocialApiClient apiClient = socialClientProvider.getClient(provider);
		return apiClient.getAccessToken(code, state);
	}

	// todo: proverId 발급 로직 구현 예정

}
