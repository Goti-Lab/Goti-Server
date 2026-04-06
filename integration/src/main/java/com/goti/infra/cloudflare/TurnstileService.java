package com.goti.infra.cloudflare;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.api.client.cloudflare.TurnstileApiClient;
import com.goti.infra.api.dto.response.cloudflare.TurnstileVerifyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnstileService {
	private final TurnstileApiClient turnstileApiClient;

	public boolean verify(String token) {
		if (token == null || token.isBlank()) {
			log.info("action=TURNSTILE_VERIFY result=FAIL reason=TOKEN_MISSING");
			return false;
		}

		try {
			TurnstileVerifyResponse response = turnstileApiClient.verify(token);
			boolean isSucceed = response != null && response.isSucceed();
			if (!isSucceed) {
				log.info(
					"action=TURNSTILE_VERIFY result=FAIL reason=VERIFICATION_REJECTED errorCodes={}",
					response != null ? response.errorCodes() : null
				);
			}
			return isSucceed;
		} catch (RestClientException e) {
			log.warn(
				"action=TURNSTILE_VERIFY result=FAIL reason=VERIFY_API_ERROR error={}",
				e.getClass().getSimpleName(),
				e
			);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		}
	}
}
