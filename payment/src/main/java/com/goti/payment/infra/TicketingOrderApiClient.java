package com.goti.payment.infra;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.constants.messages.ErrorCode;
import com.goti.config.properties.ApiEndpointProperties;
import com.goti.exception.CustomException;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.payment.dto.response.TicketingOrderListItemResponse;
import com.goti.payment.service.dto.OrderPaymentConfirmApiRequest;
import com.goti.payment.service.dto.PaymentOrderInfo;

@Component
public class TicketingOrderApiClient extends BaseRestClient implements TicketingOrderClient {

	private static final String ORDER_API = "/api/v1/orders";
	private static final String PATH_SEPARATOR = "/";
	private static final String PAYMENT_CONFIRMATIONS_PATH = "/payment-confirmations";
	private static final String PAYMENT_ORDER_PATH = "/payment-order";
	private static final String INTERNAL_ORDER_PATH = "/internal";

	public TicketingOrderApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.ticketing());
	}

	@Override
	public void confirmPayment(
		UUID orderId,
		OrderPaymentConfirmApiRequest request
	) {
		String uri = ORDER_API + PATH_SEPARATOR + orderId + PAYMENT_CONFIRMATIONS_PATH;
		postVoid(uri, request);
	}

	@Override
	public PaymentOrderInfo getPaymentOrder(UUID orderId, UUID memberId) {
		String uri = ORDER_API + PATH_SEPARATOR + orderId + PAYMENT_ORDER_PATH;
		var response = getGotiResponse(
			uri,
			null,
			Map.of("memberId", memberId),
			new ParameterizedTypeReference<ApiSuccessResponse<PaymentOrderInfo>>() {}
		);

		if (response == null) {
			throw new CustomException(ErrorCode.INTERNAL_API_INVALID_RESPONSE);
		}

		return response;
	}

	@Override
	public List<TicketingOrderListItemResponse> getOrders(
		UUID memberId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		var response = getGotiResponse(
			ORDER_API + INTERNAL_ORDER_PATH,
			null,
			createOrderQueryParams(memberId, months, startDate, endDate),
			new ParameterizedTypeReference<ApiSuccessResponse<List<TicketingOrderListItemResponse>>>() {}
		);

		if (response == null) {
			throw new CustomException(ErrorCode.INTERNAL_API_INVALID_RESPONSE);
		}

		return response;
	}

	private Map<String, Object> createOrderQueryParams(
		UUID memberId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("memberId", memberId);
		queryParams.put("months", months);
		queryParams.put("startDate", startDate);
		queryParams.put("endDate", endDate);
		return queryParams;
	}
}
