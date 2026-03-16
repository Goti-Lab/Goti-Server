package com.goti.infra;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.goti.config.properties.PaymentApiProperties;
import com.goti.dto.request.ResalePaymentRequest;

@Component
public class PaymentClient {
	private final RestClient restClient;
	private final PaymentApiProperties properties;

	public PaymentClient(RestClient restClient, PaymentApiProperties properties) {
		this.restClient = restClient;
		this.properties = properties;
	}

	public void createResalePayment(ResalePaymentRequest request) {
		restClient.post()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/resale/payments")
					.build()
					.toUri()
			)
			.body(request)
			.retrieve()
			.toBodilessEntity();
	}

	public void releaseEscrow(UUID orderId) {
		restClient.post()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/resale/payments/orders/{orderId}/release")
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.toBodilessEntity();
	}
}
