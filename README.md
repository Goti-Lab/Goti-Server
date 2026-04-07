# Goti-server

### 대규모 티켓팅 서비스 백엔드 기술스택
    - Language: Java 21
    - Framework: Spring 6.2.15 (Spring Boot 3.5.10)
    - Build: Gradle 8.14
    - Docs: SpringDoc OpenAPI (Swagger) 2.8.14

## 프로젝트 구조

```
Goti-server/
├── api/           # 모놀리식 애플리케이션 (전체 통합 bootJar)
├── common/        # 공통 예외처리, API 응답 래퍼
├── integration/   # 외부 API 연동 (OAuth, SMS, Redis, RestClient)
├── user/          # 유저 인증/인가 도메인
├── stadium/       # 경기장 도메인
├── ticketing/     # 티켓팅 도메인
├── payment/       # 결제 도메인
├── resale/        # 리세일 도메인
└── docker/        # Docker Compose 설정
```

## 로컬 개발 환경 설정

### 사전 요구사항

- Java 21
- Docker & Docker Compose

### 1. 환경변수 설정

```bash
cp .env.example .env
```

`.env` 파일을 열어 OAuth 키 등 실제 값을 채워넣습니다.

### 2. 인프라 실행 (PostgreSQL + Redis)

```bash
make db-up
```

### 3. 애플리케이션 실행

**IntelliJ:** `GotiApplication` (api 모듈) Run

**CLI:**
```bash
make build
java -jar api/build/libs/*.jar
```

### 4. 접속 확인

```
http://localhost:8080
```

### 5. 인프라 종료

```bash
make db-down
```

## MSA 모드 (서비스별 독립 실행)

각 도메인 모듈이 독립 서비스로 실행됩니다. Docker Compose로 5개 서비스 + PostgreSQL + Redis를 한번에 기동합니다.

### 서비스 포트

| 서비스 | 포트 | 스키마 |
|--------|------|--------|
| user | 8081 | user_service |
| stadium | 8082 | stadium_service |
| ticketing | 8083 | ticketing_service |
| payment | 8084 | payment_service |
| resale | 8085 | resale_service |

### MSA 실행

```bash
# 전체 시작 (빌드 + 인프라 + 5개 서비스)
make msa-up

# 상태 확인
make msa-ps

# 로그 확인
make msa-logs

# 특정 서비스만 재시작 (예: user)
make msa-restart SVC=user

# 전체 중지
make msa-down

# 볼륨 포함 정리 (DB 데이터 삭제)
make msa-clean
```

### MSA 빌드만 (Docker 없이)

```bash
make msa-build
```

## Makefile 명령어

### 모놀리식 (기본)

| 명령어 | 설명 |
|--------|------|
| `make help` | 도움말 |
| `make build` | Gradle bootJar 빌드 |
| `make test` | 전체 테스트 실행 |
| `make docker-build` | Docker 이미지 빌드 |
| `make db-up` | 인프라(PostgreSQL + Redis) 시작 |
| `make db-down` | 인프라 중지 |
| `make db-logs` | 인프라 로그 확인 |
| `make up` | 전체 스택(앱 + 인프라) 시작 |
| `make down` | 전체 스택 중지 |
| `make logs` | 전체 스택 로그 확인 |
| `make ps` | 컨테이너 상태 확인 |
| `make clean` | 볼륨 포함 전체 정리 |

### MSA

| 명령어 | 설명 |
|--------|------|
| `make msa-build` | 모듈별 bootJar 빌드 |
| `make msa-up` | 전체 서비스 시작 (5개 서비스 + DB + Redis) |
| `make msa-down` | 전체 서비스 중지 |
| `make msa-ps` | 서비스 상태 확인 |
| `make msa-logs` | 전체 서비스 로그 |
| `make msa-restart SVC=<name>` | 특정 서비스 재시작 |
| `make msa-clean` | 볼륨 포함 전체 정리 |

## 테스트

```bash
# 전체 테스트
make test

# 특정 모듈 테스트
./gradlew :user:test --no-daemon
./gradlew :stadium:test --no-daemon
```

테스트는 `.env.test` 없이도 동작합니다 (기본값 내장). `make db-up`으로 PostgreSQL이 실행 중이어야 합니다.
