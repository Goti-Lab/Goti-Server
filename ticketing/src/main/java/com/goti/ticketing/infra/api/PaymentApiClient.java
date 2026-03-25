package com.goti.ticketing.infra.api;

import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.ticketing.infra.api.dto.PaymentCancelResponse;
import com.goti.ticketing.order.dto.request.OrderPaymentCancelRequest;

@Component
public class PaymentApiClient extends BaseRestClient {
	private static final String PAYMENT_CANCEL_API = "/api/v1/payments/orders";
	private static final String PATH_SEPARATOR = "/";

	public PaymentApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.payment());
	}

	public PaymentCancelResponse cancelPayment(UUID orderId, UUID cancellationId) {
		String uri = PAYMENT_CANCEL_API + PATH_SEPARATOR + orderId + "/cancellations";
		OrderPaymentCancelRequest request = new OrderPaymentCancelRequest(cancellationId);
		return postGotiResponse(
			uri,
			request,
			new ParameterizedTypeReference<ApiSuccessResponse<PaymentCancelResponse>>() {}
		);
	}
}
