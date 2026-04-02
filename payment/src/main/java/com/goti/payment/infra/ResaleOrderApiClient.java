package com.goti.payment.infra;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.payment.dto.response.ResaleOrderListResponse;

@Component
public class ResaleOrderApiClient extends BaseRestClient implements ResaleOrderClient {
	private static final String RESALE_ORDER_API = "/api/v1/resales/orders";
	private static final String PATH_SEPARATOR = "/";

	public ResaleOrderApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.resale());
	}

	@Override
	public void completeOrder(UUID orderId, UUID paymentId) {
		String uri = RESALE_ORDER_API + PATH_SEPARATOR + orderId + PATH_SEPARATOR + "complete";
		patchVoid(uri, Map.of("paymentId", paymentId));
	}

	@Override
	public List<UUID> getTransactionIds(UUID orderId) {
		String uri = RESALE_ORDER_API + PATH_SEPARATOR + orderId + "/transactions";
		ResaleOrderListResponse response = getGotiResponse(
			uri,
			null,
			null,
			new ParameterizedTypeReference<>() {
			}
		);
		return response != null ? response.transactionIds() : List.of();
	}

	@Override
	public void completeSettlement(UUID orderId) {
		String uri = RESALE_ORDER_API + PATH_SEPARATOR + orderId + "/settled";
		patchVoid(uri, null);
	}
}
