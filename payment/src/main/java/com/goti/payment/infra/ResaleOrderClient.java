package com.goti.payment.infra;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.payment.config.properties.ResaleApiProperties;
import com.goti.payment.dto.response.ResaleOrderListResponse;

@Component
public class ResaleOrderClient {
	private final RestClient restClient;
	private final ResaleApiProperties properties;

	public ResaleOrderClient(RestClient restClient, ResaleApiProperties properties) {
		this.restClient = restClient;
		this.properties = properties;
	}

	public void completeOrder(UUID orderId, UUID paymentId) {
		restClient.patch()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/resales/orders/{orderId}/complete")
					.queryParam("paymentId", paymentId)
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.toBodilessEntity();
	}

	public List<UUID> getTransactionIds(UUID orderId) {
		ApiSuccessResponse<ResaleOrderListResponse> response = restClient.get()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/resales/orders/{orderId}/transactions")
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		return response != null ? response.getData().transactionIds() : List.of();
	}

	public void completeSettlement(UUID orderId) {
		restClient.patch()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/resales/orders/{orderId}/settled")
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.toBodilessEntity();
	}
}
