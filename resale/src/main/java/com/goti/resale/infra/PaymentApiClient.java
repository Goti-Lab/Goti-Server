package com.goti.resale.infra;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.resale.dto.request.ResalePaymentRequest;

@Component
public class PaymentApiClient extends BaseRestClient implements PaymentClient {
	private static final String RESALE_PAYMENT_API = "/api/v1/payments/resales";
	private static final String PATH_SEPARATOR = "/";

	public PaymentApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.payment());
	}

	@Override
	public void createResalePayment(ResalePaymentRequest request) {
		postVoid(RESALE_PAYMENT_API, request);
	}

	@Override
	public void releaseEscrow(UUID orderId) {
		String uri = RESALE_PAYMENT_API + "/orders" + PATH_SEPARATOR + orderId + "/release";
		patchVoid(uri, null);
	}
}
