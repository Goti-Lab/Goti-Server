#!/bin/bash
set -euo pipefail

# Usage: deploy.sh <ECR_IMAGE> <IMAGE_TAG> <AWS_REGION>
ECR_IMAGE="${1:?ECR_IMAGE required}"
IMAGE_TAG="${2:?IMAGE_TAG required}"
AWS_REGION="${3:?AWS_REGION required}"

DEPLOY_DIR="/opt/goti-server"
COMPOSE_FILE="${DEPLOY_DIR}/docker/docker-compose.deploy.yml"
ENV_FILE="${DEPLOY_DIR}/docker/.env"
SSM_PREFIX="/dev/server"

echo "===== Goti-server 배포 시작 ====="
echo "이미지: ${ECR_IMAGE}:${IMAGE_TAG}"

# --- ECR 로그인 ---
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${ECR_IMAGE%%/*}"

# --- Docker pull ---
docker pull "${ECR_IMAGE}:${IMAGE_TAG}"

# --- SSM Parameter Store에서 환경변수 로드 → .env 생성 ---
echo "SSM Parameter Store에서 환경변수 로드 중..."

PARAMS=$(aws ssm get-parameters-by-path \
  --path "${SSM_PREFIX}/" \
  --with-decryption \
  --region "${AWS_REGION}" \
  --query "Parameters[*].[Name,Value]" \
  --output text)

if [ -z "$PARAMS" ]; then
  echo "ERROR: SSM 파라미터를 찾을 수 없습니다 (prefix: ${SSM_PREFIX}/)"
  exit 1
fi

# .env 파일 생성 (600 권한)
: > "${ENV_FILE}"
chmod 600 "${ENV_FILE}"

while IFS=$'\t' read -r name value; do
  key=$(echo "$name" | sed "s|${SSM_PREFIX}/||")
  echo "${key}=${value}" >> "${ENV_FILE}"
done <<< "$PARAMS"

# Docker Compose 변수 추가
AWS_ACCOUNT_ID=$(echo "${ECR_IMAGE}" | cut -d'.' -f1)
{
  echo "AWS_ACCOUNT_ID=${AWS_ACCOUNT_ID}"
  echo "AWS_REGION=${AWS_REGION}"
  echo "IMAGE_NAME=${ECR_IMAGE#*/}"
  echo "IMAGE_TAG=${IMAGE_TAG}"
} >> "${ENV_FILE}"

echo "환경변수 ${ENV_FILE} 생성 완료"

# --- 외부 네트워크 사전 생성 (모니터링 미배포 시 대비) ---
docker network inspect goti-monitoring >/dev/null 2>&1 || docker network create goti-monitoring

# --- 기존 컨테이너 중지 및 새로 시작 ---
cd "${DEPLOY_DIR}"
# 프로젝트명 전환 감지: 기존 컨테이너가 다른 프로젝트로 관리되는 경우만 정리
OLD_PROJECT=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}' goti-postgres 2>/dev/null || true)
if [ -n "$OLD_PROJECT" ] && [ "$OLD_PROJECT" != "goti-server" ]; then
  echo "프로젝트명 전환 감지 ($OLD_PROJECT → goti-server) — 이전 스택 정리..."
  docker compose -f "${COMPOSE_FILE}" -p "$OLD_PROJECT" --env-file "${ENV_FILE}" down --remove-orphans || true
fi
docker compose -f "${COMPOSE_FILE}" -p goti-server --env-file "${ENV_FILE}" up -d

# --- 헬스체크 ---
echo "헬스체크 대기 중..."
MAX_RETRIES=20
RETRY_INTERVAL=5

for i in $(seq 1 $MAX_RETRIES); do
  if wget -qO- http://localhost:8080/actuator/health 2>/dev/null | grep -q '"status":"UP"'; then
    echo "헬스체크 성공 (시도 ${i}/${MAX_RETRIES})"
    break
  fi
  if [ "$i" -eq "$MAX_RETRIES" ]; then
    echo "ERROR: 헬스체크 실패 — ${MAX_RETRIES}회 시도 후 타임아웃"
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" logs --tail=50
    # 시크릿 삭제 후 종료
    shred -u "${ENV_FILE}" 2>/dev/null || rm -f "${ENV_FILE}"
    exit 1
  fi
  echo "  대기 중... (${i}/${MAX_RETRIES})"
  sleep $RETRY_INTERVAL
done

# --- 시크릿 삭제 ---
shred -u "${ENV_FILE}" 2>/dev/null || rm -f "${ENV_FILE}"

echo "===== 배포 완료 ====="
docker ps --filter "name=goti-server" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
