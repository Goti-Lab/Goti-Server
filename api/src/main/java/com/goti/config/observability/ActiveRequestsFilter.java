package com.goti.config.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 현재 처리 중인 HTTP 요청 수를 추적하는 서블릿 필터.
 *
 * <p>OTel Spring Boot Starter는 요청 duration(히스토그램)만 제공하고
 * active request count는 제공하지 않으므로, UpDownCounter로 직접 계측한다.</p>
 *
 * <p>Prometheus 메트릭: {@code goti_http_server_active_requests}
 * <br>레이블: {@code http_request_method} (GET, POST, ...)</p>
 *
 * <p>actuator, swagger 등 내부 경로는 계측에서 제외한다.</p>
 *
 * @see <a href="https://opentelemetry.io/docs/specs/semconv/http/http-metrics/">OTel HTTP Semantic Conventions</a>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ActiveRequestsFilter extends OncePerRequestFilter {

    private static final String METER_NAME = "goti-server";
    private static final String METRIC_NAME = "goti.http.server.active_requests";
    private static final AttributeKey<String> METHOD_KEY = AttributeKey.stringKey("http.request.method");

    /** 계측 대상에서 제외할 경로 접두사 (운영/문서용 엔드포인트) */
    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/actuator", "/swagger-ui", "/v3/api-docs"
    );

    private final LongUpDownCounter activeRequests;

    public ActiveRequestsFilter(OpenTelemetry openTelemetry) {
        this.activeRequests = openTelemetry.getMeter(METER_NAME)
                .upDownCounterBuilder(METRIC_NAME)
                .setDescription("Number of active HTTP server requests")
                .setUnit("{requests}")
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Attributes attrs = Attributes.of(METHOD_KEY, request.getMethod());
        activeRequests.add(1, attrs);
        try {
            filterChain.doFilter(request, response);
        } finally {
            activeRequests.add(-1, attrs);
        }
    }

    /** actuator, swagger 등 내부 경로는 active request 계측에서 제외 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
