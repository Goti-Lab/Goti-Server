package com.goti.service.auth.application;

import com.goti.constants.OAuthProvider;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.client.SocialClientProvider;

import com.goti.infra.api.dto.response.common.SocialStateResponse;
import com.goti.infra.cache.RedisCache;

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

	// todo : socialUserInfo 에서 받은 providerId 로 socialProvider 데이터 유무체크 및 로깅제거
	public void login(OAuthProvider provider, String code, String state) {
		validateState(provider, state);
		SocialApiClient apiClient = socialClientProvider.getClient(provider);
		String accessToken = apiClient.getAccessToken(code, state);
		log.info("accessToken :: {}", accessToken);
		var socialUserInfo = apiClient.getSocialUserInfo(accessToken);
		log.info("socialUserInfo : {}", socialUserInfo);
	}


	public SocialStateResponse issueState(OAuthProvider provider) {
		if (provider == OAuthProvider.KAKAO) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
		String state = UUID.randomUUID().toString();
		String keyParam = provider + KEY_SEPARATOR + state;

		redisCache.set(RedisKey.OAUTH_STATE, keyParam, true);

		return SocialStateResponse.of(state);
	}

	private void validateState(OAuthProvider provider, String state) {
		if (provider == OAuthProvider.KAKAO) return;

		if (state == null || state.isBlank()) {
			throw new CustomException(ErrorCode.MISSING_PARAMETER, "state");
		}
		String keyParam = provider + KEY_SEPARATOR + state;
		String stateKey = RedisKey.OAUTH_STATE.getKey(keyParam);
		if (!redisCache.consume(stateKey)) {
			throw new CustomException(ErrorCode.INVALID_STATE);
		}
	}
}
