package com.goti.ticketing.infra.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.ticketing.infra.api.dto.response.PaymentCancelResponse;
import com.goti.ticketing.infra.api.dto.response.UnsettledAmountResponse;
import com.goti.ticketing.order.dto.request.OrderPaymentCancelRequest;

@Component
public class TicketPaymentApiClient extends BaseRestClient implements TicketPaymentClient {
	private static final String PAYMENT_CANCEL_API = "/internal/payments/orders";
	private static final String PAYMENT_UNSETTLED_API = "/internal/payments/resales";
	private static final String PATH_SEPARATOR = "/";

	public TicketPaymentApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.payment());
	}

	public PaymentCancelResponse cancelPayment(UUID orderId, UUID cancellationId) {
		String uri = PAYMENT_CANCEL_API + PATH_SEPARATOR + orderId + "/cancellations";
		OrderPaymentCancelRequest request = new OrderPaymentCancelRequest(cancellationId);
		return postGotiResponse(
			uri,
			request,
			new ParameterizedTypeReference<ApiSuccessResponse<PaymentCancelResponse>>() {
			}
		);
	}

	public UnsettledAmountResponse getUnsettledAmounts(UUID userId) {
		return getGotiResponse(
			PAYMENT_UNSETTLED_API + PATH_SEPARATOR + "unsettled",
			null,
			Map.of("userId", userId),
			new ParameterizedTypeReference<>() {
			}
		);
	}
}
