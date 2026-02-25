package com.goti.config.api.interceptor;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class ExternalLoggingInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(
		HttpRequest request, byte[] body, ClientHttpRequestExecution execution
	) throws IOException {

		long start = System.nanoTime();
		ClientHttpResponse response = execution.execute(request, body);
		long tookMs = (System.nanoTime() - start) / 1_000_000;

		return response;
	}
}
