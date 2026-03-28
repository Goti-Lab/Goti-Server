package com.goti.resale.infra;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.goti.resale.config.properties.PaymentApiProperties;
import com.goti.resale.dto.request.ResalePaymentRequest;

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
					.path("/api/v1/payments/resales")
					.build()
					.toUri()
			)
			.body(request)
			.retrieve()
			.toBodilessEntity();
	}

	public void releaseEscrow(UUID orderId) {
		restClient.patch()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/payments/resales/orders/{orderId}/release")
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.toBodilessEntity();
	}
}
