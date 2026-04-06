package com.goti.infra.api.client.cloudflare;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.CloudflareTurnstileProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.infra.api.dto.request.cloudflare.TurnstileVerifyRequest;
import com.goti.infra.api.dto.response.cloudflare.TurnstileVerifyResponse;

@Component
public class TurnstileApiClient extends BaseRestClient {
	private final CloudflareTurnstileProperties properties;

	public TurnstileApiClient(RestClient.Builder builder, CloudflareTurnstileProperties properties) {
		super(builder);
		this.properties = properties;
	}

	public TurnstileVerifyResponse verify(String token) {
		TurnstileVerifyRequest request = new TurnstileVerifyRequest(
			properties.secret(),
			token
		);

		return post(
			properties.verifyUrl(),
			request.toFormData(),
			MediaType.APPLICATION_FORM_URLENCODED,
			TurnstileVerifyResponse.class
		);
	}
}
