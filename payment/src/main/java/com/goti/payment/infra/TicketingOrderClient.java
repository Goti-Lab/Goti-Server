package com.goti.payment.infra;

import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.payment.config.properties.TicketingApiProperties;
import com.goti.payment.service.dto.OrderPaymentConfirmApiRequest;
import com.goti.payment.service.dto.PaymentOrderInfo;

@Component
public class TicketingOrderClient extends BaseRestClient {
	private static final String ORDER_API = "/api/v1/orders";

	public TicketingOrderClient(RestClient.Builder builder, TicketingApiProperties properties) {
		super(builder, properties.baseUrl());
	}

	public void confirmPayment(
		UUID orderId,
		OrderPaymentConfirmApiRequest request
	) {
		String uri = String.format("%s/%s/payment-confirmations", ORDER_API, orderId);
		postVoid(uri, request);
	}

	public PaymentOrderInfo getPaymentOrder(UUID orderId, UUID memberId) {
		String uri = String.format("%s/%s/payment-order", ORDER_API, orderId);
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
}
