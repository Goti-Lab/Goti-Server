package com.goti.config.api.interceptor;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class ExternalTraceInterceptor implements ClientHttpRequestInterceptor {
	private static final String TRACE_HEADER = "X-Trace-Id";

	@Override
	public ClientHttpResponse intercept(
		HttpRequest request, byte[] body, ClientHttpRequestExecution execution
	) throws IOException {

		HttpHeaders headers = request.getHeaders();
		if (!headers.containsKey(TRACE_HEADER)) {
			headers.add(TRACE_HEADER, UUID.randomUUID().toString());
		}

		return execution.execute(request, body);
	}
}