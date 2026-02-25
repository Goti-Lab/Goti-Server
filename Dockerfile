# ===== Stage 1: Build =====
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Gradle wrapper + 설정 파일 먼저 복사 (의존성 캐시 레이어)
COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
RUN chmod +x gradlew

# 각 모듈의 build.gradle 복사 (의존성 해석용)
COPY core/build.gradle core/build.gradle
COPY common/build.gradle common/build.gradle
COPY integration/build.gradle integration/build.gradle
COPY user/build.gradle user/build.gradle
COPY stadium/build.gradle stadium/build.gradle
COPY ticketing/build.gradle ticketing/build.gradle
COPY payment/build.gradle payment/build.gradle
COPY resale/build.gradle resale/build.gradle
COPY api/build.gradle api/build.gradle

# 의존성 다운로드 (소스 변경 시 캐시 재사용)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon || true

# 전체 소스 복사
COPY . .

# bootJar 빌드
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :api:bootJar --no-daemon -x test

# ===== Stage 2: Extract (layertools) =====
FROM eclipse-temurin:21-jdk-alpine AS extract

WORKDIR /app

COPY --from=build /app/api/build/libs/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

# ===== Stage 3: Runtime =====
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S goti && adduser -S goti -G goti

WORKDIR /app

# 변경 빈도 낮은 순서대로 COPY (레이어 캐시 최적화)
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/ ./

RUN chown -R goti:goti /app
USER goti

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
