package com.goti.infra;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.goti.config.properties.ResaleApiProperties;
import com.goti.global.api.ApiSuccessResponse;

@Component
public class ResaleOrderClient {
	private final RestClient restClient;
	private final ResaleApiProperties properties;

	public ResaleOrderClient(RestClient restClient, ResaleApiProperties properties) {
		this.restClient = restClient;
		this.properties = properties;
	}

	public void completeOrder(UUID orderId, UUID paymentId) {
		restClient.post()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/resale/orders/{orderId}/complete")
					.queryParam("paymentId", paymentId)
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.toBodilessEntity();
	}

	public List<UUID> getTransactionIds(UUID orderId) {
		ApiSuccessResponse<List<UUID>> response = restClient.get()
			.uri(
				UriComponentsBuilder.fromUriString(properties.baseUrl())
					.path("/api/v1/resale/orders/{orderId}/transactions")
					.buildAndExpand(orderId)
					.toUri()
			)
			.retrieve()
			.body(new ParameterizedTypeReference<ApiSuccessResponse<List<UUID>>>() {
			});

		return response != null ? response.getData() : List.of();
	}
}
