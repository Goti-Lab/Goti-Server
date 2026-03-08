package com.goti.service.domain.auth;

import com.goti.config.jwt.JwtTokenProvider;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;
import com.goti.infra.sms.SmsProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final SmsProvider smsProvider;
	private final RedisCache redisCache;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	@Override
	public void sendSmsCode(String socialVerifyToken, String mobile) {
		jwtTokenProvider.getSocialVerifyClaims(socialVerifyToken);
		String authCode = createAuthCode();
		smsProvider.send(mobile, authCode);
		saveAuthCode(mobile, authCode);
	}

	@Override
	public void verifySmsCode(String mobile, String authCode) {
		String redisKey = RedisKey.SMS_AUTH_CODE.getKey(mobile);

		String cachedAuthCode = getCachedAuthCode(redisKey);

		if (!cachedAuthCode.equals(authCode))
			throw new CustomException(ErrorCode.AUTH_CODE_INVALID);

		if (!redisCache.consume(redisKey))
			throw new CustomException(ErrorCode.AUTH_CODE_NOT_FOUND);
	}

	private String createAuthCode() {
		int code = SECURE_RANDOM.nextInt(900000) + 100000;
		return String.valueOf(code);
	}

	private void saveAuthCode(String mobile, String authCode) {
		redisCache.set(RedisKey.SMS_AUTH_CODE, mobile, authCode);
	}

	private String getCachedAuthCode(String key) {
		String cachedCode = redisCache.get(
			key, String.class
		);
		if (cachedCode == null)
			throw new CustomException(ErrorCode.AUTH_CODE_NOT_FOUND);
		return cachedCode;
	}

}
