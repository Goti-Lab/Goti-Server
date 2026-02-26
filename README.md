# Goti-server

대규모 티켓팅 서비스 백엔드 (Spring Boot 3.5, Java 21, Gradle 8.14)

## 프로젝트 구조

```
Goti-server/
├── api/           # 메인 애플리케이션 (bootJar)
├── core/          # 도메인 엔티티, 상수
├── common/        # 공통 예외처리, API 응답 래퍼
├── integration/   # 외부 API 연동 (OAuth, Redis)
├── user/          # 유저 인증/인가
├── stadium/       # 경기장 도메인
├── ticketing/     # 티켓팅 도메인
├── payment/       # 결제 도메인
└── resale/        # 리세일 도메인
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

## Makefile 명령어

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

## 참고

- 모든 모듈은 `api` 모듈 하나로 통합 실행됩니다 (단일 포트 8080)
- 각 모듈의 `application.yml`과 `Application.java`는 MSA 전환 대비 구조이며, 현재는 `api` 모듈의 설정만 적용됩니다
