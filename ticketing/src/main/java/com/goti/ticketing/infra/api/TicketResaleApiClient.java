package com.goti.ticketing.infra.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.ticketing.infra.api.dto.response.ResaleListingMyPageCountResponse;

@Component
public class TicketResaleApiClient extends BaseRestClient implements TicketResaleClient {
	private final static String RESALE_GET_API = "/api/v1/internal/resales";
	private final static String PATH_SEPARATOR = "/";

	public TicketResaleApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.resale());
	}

	@Override
	public ResaleListingMyPageCountResponse getMySales(UUID userId) {
		String uri = RESALE_GET_API + PATH_SEPARATOR + "listings" + PATH_SEPARATOR + "count" + PATH_SEPARATOR + "listing";
		return getGotiResponse(
			uri,
			null,
			Map.of("sellerId", userId),
			new ParameterizedTypeReference<>() {
			}
		);
	}
}
