package com.goti.config.api;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.goti.config.properties.RestClientProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.goti.config.api.interceptor.ExternalLoggingInterceptor;
import com.goti.config.api.interceptor.ExternalTraceInterceptor;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

	private final RestClientProperties properties;
	private final ExternalTraceInterceptor traceInterceptor;
	private final ExternalLoggingInterceptor loggingInterceptor;

	private static final String TRACE_HEADER = "X-Trace-Id";

	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(properties.connectTimeout()))
			.build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(properties.readTimeout()));

		return builder
			.requestFactory(requestFactory)
			.requestInterceptor(traceInterceptor)
			.requestInterceptor(loggingInterceptor)
			.defaultStatusHandler(
				HttpStatusCode::isError,
				(request, response) -> handleError(request, response)
			)
			.build();
	}

	private void handleError(HttpRequest request, ClientHttpResponse response) throws IOException {
		int status = response.getStatusCode().value();
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		String traceId = request.getHeaders().getFirst(TRACE_HEADER);

		log.error(
			"[External API Error] traceId={}, method={}, uri={}, status={}, body={}",
			traceId,
			request.getMethod(),
			request.getURI(),
			status,
			body
		);

		throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
			new RuntimeException(String.format("외부 API 오류: %s %s → %d",
				request.getMethod(), request.getURI(), status)));
	}
}
