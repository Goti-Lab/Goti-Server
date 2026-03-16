package com.goti.payment.infra;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.goti.payment.config.properties.TicketingApiProperties;
import com.goti.payment.service.dto.OrderPaymentConfirmApiRequest;

@Component
public class TicketingOrderClient {
	private final RestClient restClient;
	private final TicketingApiProperties properties;

	public TicketingOrderClient(RestClient restClient, TicketingApiProperties properties) {
		this.restClient = restClient;
		this.properties = properties;
	}

	public void confirmPayment(
		UUID orderId,
		OrderPaymentConfirmApiRequest request
	) {
		restClient.post()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/orders/{orderId}/payment-confirmations")
					.buildAndExpand(orderId)
					.toUri()
			)
			.body(request)
			.retrieve()
			.toBodilessEntity();
	}
}
