package com.goti.payment.infra;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.payment.dto.response.ResaleOrderListResponse;
import com.goti.payment.dto.response.ResalePurchaseListItemResponse;

import jakarta.servlet.http.HttpServletRequest;

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
		patchVoid(uri, getHeaders(), Map.of("paymentId", paymentId));
	}

	@Override
	public List<UUID> getTransactionIds(UUID orderId) {
		String uri = RESALE_ORDER_API + PATH_SEPARATOR + orderId + PATH_SEPARATOR + "transactions";
		ResaleOrderListResponse response = getGotiResponse(
			uri,
			getHeaders(),
			null,
			new ParameterizedTypeReference<>() {
			}
		);
		return response != null ? response.transactionIds() : List.of();
	}

	@Override
	public void completeSettlement(UUID orderId) {
		String uri = RESALE_ORDER_API + PATH_SEPARATOR + orderId + PATH_SEPARATOR + "settled";
		patchVoid(uri, getHeaders(), null);
	}

	@Override
	public List<ResalePurchaseListItemResponse> getPurchases(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		List<ResalePurchaseListItemResponse> response = getGotiResponse(
			RESALE_ORDER_API + "/purchases",
			getHeaders(),
			createPurchaseQueryParams(buyerId, months, startDate, endDate),
			new ParameterizedTypeReference<>() {}
		);
		return response != null ? response : List.of();
	}

	private Map<String, Object> createPurchaseQueryParams(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("buyerId", buyerId);
		queryParams.put("months", months);
		queryParams.put("startDate", startDate);
		queryParams.put("endDate", endDate);
		return queryParams;
	}

	private Map<String, String> getHeaders() {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			HttpServletRequest request = attributes.getRequest();
			String bearerToken = request.getHeader("Authorization");

			if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
				return createBearerHeader(bearerToken.substring(7)); // BaseRestClient의 헬퍼 메서드 활용
			}
		}
		return null;
	}
}
