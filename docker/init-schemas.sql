-- MSA 서비스별 스키마 분리
-- docker-compose에서 PostgreSQL 초기화 시 자동 실행

CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS stadium_service;
CREATE SCHEMA IF NOT EXISTS ticketing_service;
CREATE SCHEMA IF NOT EXISTS payment_service;
CREATE SCHEMA IF NOT EXISTS resale_service;
