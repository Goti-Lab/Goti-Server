.PHONY: help build test docker-build db-up db-down db-logs up down logs ps clean \
       msa-up msa-down msa-logs msa-ps msa-restart msa-build \
       run-user run-stadium run-ticketing run-payment run-resale run-queue

COMPOSE_INFRA := docker compose -f docker/docker-compose.yml
COMPOSE_ALL   := docker compose -f docker/docker-compose.yml -f docker/docker-compose.app.yml
COMPOSE_MSA   := docker compose -f docker/docker-compose.yml -f docker/docker-compose.msa.yml

# MSA 서비스별 포트 (docker-compose.msa.yml과 동일)
PORT_USER     := 8081
PORT_STADIUM  := 8082
PORT_TICKETING:= 8083
PORT_PAYMENT  := 8084
PORT_RESALE   := 8085
PORT_QUEUE    := 8086

help: ## 도움말
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-18s\033[0m %s\n", $$1, $$2}'

build: ## Gradle bootJar 빌드
	./gradlew :api:bootJar --no-daemon -x test

test: ## 전체 테스트 실행
	./gradlew test --no-daemon

docker-build: ## Docker 이미지 빌드
	DOCKER_BUILDKIT=1 docker build -t goti-server:local .

db-up: ## 인프라(PostgreSQL + Redis) 시작
	$(COMPOSE_INFRA) up -d

db-down: ## 인프라 중지
	$(COMPOSE_INFRA) down

db-logs: ## 인프라 로그 확인
	$(COMPOSE_INFRA) logs -f

up: ## 전체 스택(앱 + 인프라) 시작
	$(COMPOSE_ALL) up -d --build

down: ## 전체 스택 중지
	$(COMPOSE_ALL) down

logs: ## 전체 스택 로그 확인
	$(COMPOSE_ALL) logs -f

ps: ## 컨테이너 상태 확인
	$(COMPOSE_ALL) ps

clean: ## 볼륨 포함 전체 정리
	$(COMPOSE_ALL) down -v --remove-orphans

# === MSA (Docker Compose) ===

msa-build: ## [MSA] 모듈별 bootJar 빌드 (Gradle)
	./gradlew $(foreach m,user stadium ticketing payment resale queue,:$(m):bootJar) -Pmsa --no-daemon -x test

msa-up: ## [MSA] Docker로 전체 서비스 시작 (5개 서비스 + DB + Redis)
	$(COMPOSE_MSA) up -d --build

msa-down: ## [MSA] 전체 서비스 중지
	$(COMPOSE_MSA) down

msa-logs: ## [MSA] 전체 서비스 로그
	$(COMPOSE_MSA) logs -f

msa-ps: ## [MSA] 서비스 상태 확인
	$(COMPOSE_MSA) ps

msa-restart: ## [MSA] 특정 서비스 재시작 (예: make msa-restart SVC=user)
	$(COMPOSE_MSA) up -d --build $(SVC)

msa-clean: ## [MSA] 볼륨 포함 전체 정리
	$(COMPOSE_MSA) down -v --remove-orphans

# === MSA (로컬 개별 서비스 실행 — bootRun) ===

run-user: ## [MSA] user 서비스 로컬 실행 (port $(PORT_USER))
	SERVER_PORT=$(PORT_USER) ./gradlew :user:bootRun -Pmsa --no-daemon

run-stadium: ## [MSA] stadium 서비스 로컬 실행 (port $(PORT_STADIUM))
	SERVER_PORT=$(PORT_STADIUM) ./gradlew :stadium:bootRun -Pmsa --no-daemon

run-ticketing: ## [MSA] ticketing 서비스 로컬 실행 (port $(PORT_TICKETING))
	SERVER_PORT=$(PORT_TICKETING) ./gradlew :ticketing:bootRun -Pmsa --no-daemon

run-payment: ## [MSA] payment 서비스 로컬 실행 (port $(PORT_PAYMENT))
	SERVER_PORT=$(PORT_PAYMENT) TICKETING_BASE_URL=http://localhost:$(PORT_TICKETING) ./gradlew :payment:bootRun -Pmsa --no-daemon

run-resale: ## [MSA] resale 서비스 로컬 실행 (port $(PORT_RESALE))
	SERVER_PORT=$(PORT_RESALE) ./gradlew :resale:bootRun -Pmsa --no-daemon

run-queue: ## [MSA] queue 서비스 로컬 실행 (port $(PORT_QUEUE))
	SERVER_PORT=$(PORT_QUEUE) ./gradlew :queue:bootRun -Pmsa --no-daemon
