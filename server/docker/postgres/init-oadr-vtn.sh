#!/bin/bash
set -e

# PostgreSQL 15 부터 public 스키마의 CREATE 권한이 모든 사용자에게 열려 있지 않다.
# GRANT ALL ON DATABASE 로는 스키마 권한이 안 생겨서 테이블 생성이 "permission denied for schema public" 으로 막힌다.
# DB 소유자를 앱 사용자로 만들면 public 스키마(소유자 pg_database_owner)에 테이블을 만들 수 있다
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER "oadr-vtn20b" WITH ENCRYPTED PASSWORD 'supersecure';
    CREATE DATABASE "oadr-vtn20b" OWNER "oadr-vtn20b";
    GRANT ALL PRIVILEGES ON DATABASE "oadr-vtn20b" TO "oadr-vtn20b";
EOSQL