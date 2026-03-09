package com.goti.config.observability;

import com.zaxxer.hikari.HikariDataSource;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.hikaricp.v3_0.HikariTelemetry;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HikariCP 커넥션 풀 메트릭을 OTel로 직접 계측하는 설정.
 *
 * <p>Micrometer 브릿지 없이 OTel DB Pool 시맨틱 컨벤션으로 메트릭을 수집한다.</p>
 *
 * <p>수집 메트릭 (Prometheus 변환명):
 * <ul>
 *   <li>{@code db_client_connections_usage} — active/idle 커넥션 수</li>
 *   <li>{@code db_client_connections_max} — 최대 커넥션 수</li>
 *   <li>{@code db_client_connections_pending_requests} — 대기 중 스레드</li>
 *   <li>{@code db_client_connections_timeouts_total} — 타임아웃 횟수</li>
 *   <li>{@code db_client_connections_create_time} — 커넥션 생성 시간</li>
 *   <li>{@code db_client_connections_wait_time} — 커넥션 획득 대기 시간</li>
 *   <li>{@code db_client_connections_use_time} — 커넥션 사용 시간</li>
 * </ul>
 */
@Configuration
public class HikariOtelConfig {

    @Bean
    public BeanPostProcessor hikariMetricsPostProcessor(OpenTelemetry openTelemetry) {
        HikariTelemetry telemetry = HikariTelemetry.create(openTelemetry);
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof HikariDataSource ds) {
                    ds.setMetricsTrackerFactory(telemetry.createMetricsTrackerFactory());
                }
                return bean;
            }
        };
    }
}
