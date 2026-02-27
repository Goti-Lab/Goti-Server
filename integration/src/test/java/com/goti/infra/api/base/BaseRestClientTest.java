package com.goti.infra.api.base;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ActiveProfiles({"test", "integration"})
@SpringBootTest
public class BaseRestClientTest {

	@Autowired
	RestClient restClient;

	static final String BASE_URL = "https://jsonplaceholder.typicode.com";

	TestApiClient testApiClient;

	static class TestApiClient extends BaseRestClient {
		public TestApiClient(RestClient restClient) {
			super(restClient.mutate());
		}

		@Override
		protected <T> T get(
			String path,
			Map<String, String> headers,
			Map<String, ?> queryParams,
			Class<T> responseType
		) {
			return super.get(path, headers, queryParams, responseType);
		}
	}

	@BeforeEach
	void setUp() {
		testApiClient = new TestApiClient(restClient);
	}

	@Test
	void restClient_get_요청_성공_테스트() {
		Map<String, String> query = Map.of("postId", "2");
		String result = testApiClient.get(
			BASE_URL + "/comments",
			null,
			query,
			String.class
		);
		assertNotNull(result);
		log.info("Test Result :: {}", result);
	}

	@Test
	void restClient_get_요청_실패_테스트() {

		CustomException exception = assertThrows(CustomException.class, () -> {
			testApiClient.get(
				BASE_URL + "/posts/99999999",
				null,
				null,
				String.class
			);
		});
		assertThat(exception.error()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	@Test
	void restClient_post_요청_성공_테스트() {
		Map<String, Object> body = Map.of(
			"title", "foo",
			"body", "bar",
			"userId", 1
		);
		String result = testApiClient.post(
			BASE_URL + "/posts",
			body,
			String.class
		);
		assertNotNull(result);
		log.info("result :: {}", result);
	}
}
