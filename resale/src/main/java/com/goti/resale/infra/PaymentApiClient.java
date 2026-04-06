package com.goti.resale.infra;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.resale.dto.request.ResalePaymentRequest;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class PaymentApiClient extends BaseRestClient implements PaymentClient {
	private static final String RESALE_PAYMENT_API = "/api/v1/payments/resales";
	private static final String PATH_SEPARATOR = "/";

	public PaymentApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.payment());
	}

	@Override
	public void createResalePayment(ResalePaymentRequest request) {
		postVoid(RESALE_PAYMENT_API, getHeaders(), request);
	}

	@Override
	public void releaseEscrow(UUID orderId) {
		String uri = RESALE_PAYMENT_API + "/orders" + PATH_SEPARATOR + orderId + "/release";
		patchVoid(uri, getHeaders(), null);
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
