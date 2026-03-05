package com.goti.service.auth.application;

import com.goti.config.jwt.JwtTokenProvider;
import com.goti.constants.OAuthProvider;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.user.MemberEntity;
import com.goti.dto.response.SocialVerifyResponse;
import com.goti.exception.CustomException;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.client.SocialClientProvider;
import com.goti.infra.api.dto.response.common.SocialStateResponse;
import com.goti.infra.api.dto.response.common.SocialUserInfoResponse;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;

import com.goti.service.domain.user.SocialProviderService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

	private final SocialClientProvider socialClientProvider;
	private final SocialProviderService socialProviderService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisCache redisCache;

	private static final String KEY_SEPARATOR = ":";
	private static final String PROVIDER_ID_KEY = "provider_id";
	private static final String PROVIDER_TYPE_KEY = "provider_type";

	public SocialStateResponse issueState(OAuthProvider provider) {
		if (provider == OAuthProvider.KAKAO) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
		String state = UUID.randomUUID().toString();
		String keyParam = provider + KEY_SEPARATOR + state;

		redisCache.set(RedisKey.OAUTH_STATE, keyParam, true);

		return SocialStateResponse.of(state);
	}

	public SocialVerifyResponse verify(OAuthProvider provider, String authCode, String state) {
		validateState(provider, state);
		SocialApiClient apiClient = socialClientProvider.getClient(provider);
		String socialAccessToken = apiClient.getAccessToken(authCode, state);
		SocialUserInfoResponse socialUserInfo = apiClient.getSocialUserInfo(socialAccessToken);
		String providerId = socialUserInfo.providerId();
		boolean isRegistered = socialProviderService.findByProviderIdAndProvider(
			providerId, provider
		).isPresent();

		String socialVerifyToken = jwtTokenProvider.createSocialVerifyToken(
			provider, providerId
		);
		return SocialVerifyResponse.of(isRegistered, socialVerifyToken);
	}

	public Pair<String, String> login(String socialVerifyToken) {
		Claims claims = jwtTokenProvider.getSocialVerifyClaims(socialVerifyToken);
		String providerId = claims.get(PROVIDER_ID_KEY, String.class);
		OAuthProvider provider = OAuthProvider.valueOf(claims.get(PROVIDER_TYPE_KEY, String.class));
		MemberEntity member = socialProviderService.findMemberBySocialInfo(providerId, provider)
			.orElseThrow(
				() ->{
					log.error(
						"Member not found after social verify - providerId: {}, provider: {}", providerId, provider
					);
					return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
				}
			);
		String accessToken = jwtTokenProvider.create(
			member.getId(),
			member.getMobile(),
			member.getRole()
		);
		return Pair.of(accessToken, "");
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
