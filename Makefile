.PHONY: help build test docker-build db-up db-down db-logs up down logs ps clean \
       msa-build msa-run msa-stop

COMPOSE_INFRA := docker compose -f docker/docker-compose.yml
COMPOSE_ALL   := docker compose -f docker/docker-compose.yml -f docker/docker-compose.app.yml

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

# === MSA 테스트 ===
MSA_MODULES := user stadium ticketing payment resale
MSA_PORTS   := 8081 8082 8083 8084 8085

msa-build: ## [MSA] 모듈별 bootJar 빌드
	./gradlew $(foreach m,$(MSA_MODULES),:$(m):bootJar) -Pmsa --no-daemon -x test

msa-run: db-up msa-build ## [MSA] 인프라 + 모듈별 독립 실행
	@echo "=== MSA 모드 시작 ==="
	$(eval PAIRS := $(join $(MSA_MODULES),$(addprefix :,$(MSA_PORTS))))
	@$(foreach p,$(PAIRS),\
		$(eval M := $(word 1,$(subst :, ,$(p))))\
		$(eval P := $(word 2,$(subst :, ,$(p))))\
		echo "  $(M) → localhost:$(P)" && \
		SERVER_PORT=$(P) java -jar $(M)/build/libs/$(M)-0.0.1-SNAPSHOT.jar &\
	)
	@echo "=== 전체 모듈 실행 중 ==="

msa-stop: ## [MSA] 모듈 프로세스 전체 종료
	@echo "=== MSA 프로세스 종료 ==="
	@pkill -f 'goti.*SNAPSHOT.jar' || true
	@echo "종료 완료"
