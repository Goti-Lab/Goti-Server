package com.goti.infra.api.base;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public abstract class BaseRestClient {
	protected final RestClient restClient;

	protected BaseRestClient(RestClient.Builder builder) {
		this.restClient = builder
			.defaultHeaders(headers -> {
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
			})
			.build();
	}

	private UriBuilder getActualUriBuilder(String uri, UriBuilder defaultBuilder) {
		return uri.startsWith("http")
			? UriComponentsBuilder.fromUriString(uri)
			: defaultBuilder.path(uri);
	}

	private MultiValueMap<String, String> toParams(Map<String, ?> queryParams) {
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		if (queryParams != null) {
			queryParams.forEach((k, v) -> {
				if (v != null) multiValueMap.add(k, String.valueOf(v));
			});
		}
		return multiValueMap;
	}

	protected <T> T get(
		String uri,
		Map<String, String> headers,
		Map<String, ?> queryParams,
		Class<T> responseType
	) {
		return restClient.get()
			.uri(uriBuilder -> {
				UriBuilder builder = getActualUriBuilder(uri, uriBuilder);
				if (queryParams != null) {
					builder.queryParams(toParams(queryParams));
				}
				return builder.build();
			})
			.headers(header -> {
				if (headers != null) headers.forEach(header::add);
			})
			.retrieve()
			.body(responseType);
	}

	protected <T> T post(
		String uri,
		Object body,
		MediaType contentType,
		Class<T> responseType
	) {
		return restClient.post()
			.uri(uriBuilder -> getActualUriBuilder(uri, uriBuilder).build())
			.headers(header -> {
				if (contentType != null)
					header.setContentType(contentType);
			})
			.body(
				body instanceof Map ? toParams((Map<String, ?>) body) : body
			)
			.retrieve()
			.body(responseType);
	}

	protected <T> T post(String uri, Object body, Class<T> responseType) {
		return post(uri, body, null, responseType);
	}

	protected <T> T put(String uri, Object body, Class<T> responseType) {
		return restClient.put().uri(
			uriBuilder -> getActualUriBuilder(uri, uriBuilder).build()
			)
			.body(body)
			.retrieve()
			.body(responseType);
	}

	protected <T> T delete(
		String uri,
		Map<String, ?> queryParams,
		Class<T> responseType
	) {
		return restClient.delete()
			.uri(uriBuilder -> {
				UriBuilder builder = getActualUriBuilder(uri, uriBuilder);
				if (queryParams != null) {
					builder.queryParams(toParams(queryParams));
				}
				return builder.build();
			})
			.retrieve()
			.body(responseType);
	}
}