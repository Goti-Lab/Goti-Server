package com.goti.config.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ActiveRequestsFilter extends OncePerRequestFilter {

    private final LongUpDownCounter activeRequests;

    public ActiveRequestsFilter(OpenTelemetry openTelemetry) {
        this.activeRequests = openTelemetry.getMeter("goti-server")
                .upDownCounterBuilder("http.server.active_requests")
                .setDescription("Number of active HTTP server requests")
                .setUnit("{requests}")
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        activeRequests.add(1);
        try {
            filterChain.doFilter(request, response);
        } finally {
            activeRequests.add(-1);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}
