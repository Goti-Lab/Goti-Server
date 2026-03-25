package com.goti.payment.infra;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;

import com.goti.payment.dto.response.GameIdResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class OrderClient extends BaseRestClient {

	private final static String ORDER_DETAIL_API = "/api/v1/orders";
	private final static String PATH_SEPARATOR = "/";

	public OrderClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.stadium());
	}

	public GameIdResponse orderDetail(UUID orderId) {
		String uri = ORDER_DETAIL_API + PATH_SEPARATOR + orderId;
		return getGotiResponse(
			uri,
			null,
			null,
			new ParameterizedTypeReference<ApiSuccessResponse<GameIdResponse>>() {}
		);
	}


}
