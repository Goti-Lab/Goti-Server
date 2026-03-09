package com.goti.config.observability;

import com.zaxxer.hikari.HikariDataSource;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.hikaricp.v3_0.HikariTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HikariCP 커넥션 풀 메트릭을 OTel로 직접 계측하는 설정.
 *
 * <p>Micrometer 브릿지 없이 OTel DB Pool 시맨틱 컨벤션으로 메트릭을 수집한다.</p>
 *
 * <p>수집 메트릭 (Prometheus 변환명):</p>
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

    private static final Logger log = LoggerFactory.getLogger(HikariOtelConfig.class);

    /**
     * static 팩토리: Config 클래스 조기 초기화 방지.
     * ObjectProvider: OpenTelemetry 빈 지연 로딩으로 BeanPostProcessor 경고 제거.
     * postProcessBeforeInitialization: OTel DataSourcePostProcessor가 프록시로 감싸기 전에 실행.
     */
    @Bean
    static BeanPostProcessor hikariMetricsPostProcessor(ObjectProvider<OpenTelemetry> openTelemetryProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof HikariDataSource ds) {
                    OpenTelemetry openTelemetry = openTelemetryProvider.getIfAvailable();
                    if (openTelemetry != null) {
                        HikariTelemetry telemetry = HikariTelemetry.create(openTelemetry);
                        ds.setMetricsTrackerFactory(telemetry.createMetricsTrackerFactory());
                        log.info("OTel HikariCP metrics attached to {}", beanName);
                    } else {
                        log.warn("OpenTelemetry not available, skipping HikariCP metrics for {}", beanName);
                    }
                }
                return bean;
            }
        };
    }
}
