package com.goti.payment.infra;

import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.payment.config.properties.TicketingApiProperties;
import com.goti.payment.service.dto.OrderPaymentConfirmApiRequest;
import com.goti.payment.service.dto.PaymentOrderInfo;

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

	public PaymentOrderInfo getPaymentOrder(UUID orderId, UUID memberId) {
		ApiSuccessResponse<PaymentOrderInfo> response = restClient.get()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/orders/{orderId}/payment-order")
					.queryParam("memberId", memberId)
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.body(new ParameterizedTypeReference<ApiSuccessResponse<PaymentOrderInfo>>() {
			});

		if (response == null || response.getData() == null) {
			throw new IllegalStateException("ticketing 주문 조회 응답이 비어 있습니다.");
		}

		return response.getData();
	}
}
