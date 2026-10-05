#!/bin/sh
# 개발 서버에서 VTN 스택(postgres, rabbitmq, openfire, vtn20b)을 도커로 다룬다.
# 서버 디렉토리 모양: bin/run.sh, config/oadr.env, docker-compose.yml, images/oadr-images.tar.gz
# 이미지 묶음은 로컬에서 deploy/oadr/build.sh 로 만든다.
#
# 사용법: bin/run.sh <command> [인자]
#   start            띄운다. 순서는 compose 의 depends_on(healthcheck)이 맞춘다
#   stop             내린다. DB 데이터(PGDATA_DIR)는 남는다
#   restart          stop 하고 start
#   status           상태
#   logs [service]   로그(bin/run.sh logs vtn20b)
#   load [file]      이미지 묶음을 넣는다. 기본은 images/oadr-images.tar.gz
#   update [file]    load 하고 새 이미지로 다시 띄운다(DB 는 남는다). 배포할 때 이걸 쓴다
#   backup           VTN, Openfire DB 를 BACKUP_DIR/<시각>/ 에 덤프한다(pg_dump -Fc)
#   restore <dir>    backup 디렉토리로 되돌린다. vtn20b, openfire 를 잠깐 내렸다 올린다
#   reset-db         DB 데이터를 비운다. 마켓 컨텍스트, VEN, 이벤트가 다 없어진다
#   cert             VTN 서버 인증서를 VTN_HOST 로 다시 만든다(start 는 없거나 VTN_HOST 가 바뀌었을 때만 만든다.
#                    VTN_HOST=auto 면 서버 IP 가 바뀐 것도 바뀐 것으로 본다)
set -eu

BIN_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
# docker-compose.yml 이 있는 곳. config/oadr.env 의 상대 경로도 여기 기준이다
BASE_DIR=$(CDPATH= cd -- "$BIN_DIR/.." && pwd)
ENV_FILE="$BASE_DIR/config/oadr.env"
COMPOSE_FILE="$BASE_DIR/docker-compose.yml"
IMAGES_FILE="$BASE_DIR/images/oadr-images.tar.gz"
# 백업, 복원하는 DB. postgres 이미지의 init 스크립트(docker/postgres/init-*.sh)가 만든다
DATABASES="oadr-vtn20b oadr-openfire"

[ -f "$ENV_FILE" ] || { echo "설정 파일이 없다: $ENV_FILE"; exit 1; }
set -a
. "$ENV_FILE"
set +a

# config/oadr.env 의 상대 경로를 BASE_DIR 기준 절대 경로로
abs() {
  case "$1" in
    /*) echo "$1" ;;
    *)  echo "$BASE_DIR/${1#./}" ;;
  esac
}
PGDATA_PATH=$(abs "$PGDATA_DIR")
BACKUP_PATH=$(abs "$BACKUP_DIR")
LOG_PATH=$(abs "$LOG_DIR")
KEYSTORE_PATH=$(abs "$KEYSTORE_DIR")
# 이 서버의 IP. 밖으로 나가는 기본 경로에 쓰는 주소를 고른다(docker0 같은 브리지 주소를 피한다).
# ip 명령이 없거나 기본 경로가 없으면 hostname -I 의 첫 번째 주소
detect_ip() {
  found=$(ip -4 route get 1.1.1.1 2>/dev/null \
    | awk '{ for (i = 1; i < NF; i++) if ($i == "src") { print $(i + 1); exit } }')
  [ -n "$found" ] || found=$(hostname -I 2>/dev/null | awk '{ print $1 }')
  echo "$found"
}

# VTN_HOST 의 auto 를 이 서버 IP 로 바꾼다. 비어 있으면 auto 로 본다.
# 서버 IP 가 바뀌면 바뀐 값이 keystore 의 vtn.host 와 달라져서 다음 start 때 인증서를 새 IP 로 다시 만든다.
# 안내는 표준 에러로 낸다(표준 출력은 VTN_HOST 값으로 받는다)
resolve_hosts() {
  out=""
  for host in $(echo "$1" | tr ',' ' '); do
    if [ "$host" = auto ]; then
      host=$(detect_ip)
      [ -n "$host" ] || { echo "VTN_HOST=auto 인데 이 서버 IP 를 못 찾았다. config/oadr.env 의 VTN_HOST 에 직접 넣는다" >&2; exit 1; }
    fi
    out="${out:+$out,}$host"
  done
  echo "$out"
}
VTN_HOST=$(resolve_hosts "${VTN_HOST:-auto}")

# 첫 번째 VTN_HOST. 안내 문구에만 쓴다
MAIN_HOST="${VTN_HOST%%,*}"
# docker-compose.yml 의 이미지. 서버에는 load(update)로 넣는다. 레지스트리에서 받지 않는다
IMAGES="openadr/postgres:latest openadr/rabbitmq:latest openadr/openfire:latest openadr/vtn20b:latest"

# VTN_HOST 에는 이름이나 IP 만 넣는다. 포트(VTN_PORT)나 https:// 를 넣으면 인증서 이름이 틀어진다
for host in $(echo "$VTN_HOST" | tr ',' ' '); do
  case "$host" in
    *:*|*/*) echo "VTN_HOST 에는 이름이나 IP 만 넣는다(포트는 VTN_PORT): $host"; exit 1 ;;
  esac
done

COMMAND="${1:-status}"
[ $# -gt 0 ] && shift

compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

# 이미지가 서버에 다 있는지. 없으면 load(update)를 먼저 하라고 멈춘다
require_images() {
  for image in $IMAGES; do
    docker image inspect "$image" >/dev/null 2>&1 \
      || { echo "이미지가 없다: $image. 먼저 bin/run.sh update 로 images/oadr-images.tar.gz 를 넣는다"; exit 1; }
  done
}

# VTN 서버 인증서에 넣을 이름(SAN). vtn.oadr.com(도커 안 Openfire 가 이 이름으로 VTN 에 붙는다), localhost, 127.0.0.1 은 늘 넣는다.
# 숫자와 점만 있으면 IP, 아니면 DNS 이름으로 넣는다
san() {
  names="DNS:vtn.oadr.com,DNS:localhost,IP:127.0.0.1"
  for host in $(echo "$VTN_HOST" | tr ',' ' '); do
    case "$host" in
      vtn.oadr.com|localhost|127.0.0.1) ;;
      *[!0-9.]*) names="$names,DNS:$host" ;;
      *)         names="$names,IP:$host" ;;
    esac
  done
  echo "$names"
}

# VTN 서버 인증서(vtn.key, vtn.crt)를 KEYSTORE_DIR 에 만든다.
# 이미지 안 CA(oadr.com)로 서명한다. 관리자 화면에서 VEN 을 만들 때 받는 VEN 인증서, CA 와 같은 CA 라 짝이 맞는다.
# CA 개인 키는 임시 디렉토리에만 꺼내고 끝나면 지운다
# 단계마다 실패를 직접 본다. sh 는 if, && 안에서 부른 함수 안에서 set -e 가 꺼져서, 실패해도 그냥 다음 줄로 간다
make_cert() {
  command -v openssl >/dev/null 2>&1 || { echo "openssl 이 없다. 설치하고 다시 돌린다"; exit 1; }
  require_images
  mkdir -p "$KEYSTORE_PATH" || exit 1
  tmp=$(mktemp -d) || exit 1
  trap 'rm -rf "$tmp"' EXIT
  docker run --rm --entrypoint cat openadr/vtn20b:latest /opt/oadr-vtn20b/cert/oadr.com.key > "$tmp/ca.key" \
    || { echo "이미지에서 CA 키를 못 꺼냈다"; exit 1; }
  docker run --rm --entrypoint cat openadr/vtn20b:latest /opt/oadr-vtn20b/cert/oadr.com.crt > "$tmp/ca.crt" \
    || { echo "이미지에서 CA 인증서를 못 꺼냈다"; exit 1; }
  names=$(san)
  cat > "$tmp/openssl.conf" <<CONF
[ req ]
distinguished_name = dn
req_extensions     = ext
prompt             = no
[ dn ]
C  = FR
ST = Paris
L  = Paris
O  = Avob
OU = Avob
CN = vtn.oadr.com
[ ext ]
subjectAltName = $names
CONF
  openssl req -new -newkey rsa:2048 -nodes -keyout "$tmp/vtn.key" -out "$tmp/vtn.csr" -config "$tmp/openssl.conf" \
    || { echo "VTN 키, CSR 을 못 만들었다"; exit 1; }
  openssl x509 -req -sha256 -days 3650 -in "$tmp/vtn.csr" -CA "$tmp/ca.crt" -CAkey "$tmp/ca.key" \
    -set_serial "0x$(openssl rand -hex 8)" -extensions ext -extfile "$tmp/openssl.conf" -out "$tmp/vtn.crt" \
    || { echo "VTN 인증서에 서명하지 못했다"; exit 1; }
  # 다 만든 뒤에만 KEYSTORE_DIR 에 넣는다(중간에 실패하면 예전 것이 그대로 남는다)
  cp "$tmp/vtn.key" "$tmp/vtn.crt" "$tmp/ca.crt" "$KEYSTORE_PATH/" || { echo "KEYSTORE_DIR 에 못 넣었다"; exit 1; }
  chmod 600 "$KEYSTORE_PATH/vtn.key"
  # 어떤 VTN_HOST 로 만들었는지 남긴다. 바뀌면 start 가 다시 만든다
  echo "$VTN_HOST" > "$KEYSTORE_PATH/vtn.host"
  rm -rf "$tmp"
  trap - EXIT
  echo "VTN 서버 인증서를 만들었다: $KEYSTORE_PATH/vtn.crt ($names)"
}

# 인증서가 없거나 VTN_HOST 가 바뀌었을 때만 만든다. 만들었으면 RENEWED=1.
# 조건(&&, if) 안에서 부르지 않는다. 그러면 안의 set -e 가 꺼진다
RENEWED=0
ensure_cert() {
  if [ -f "$KEYSTORE_PATH/vtn.crt" ] && [ -f "$KEYSTORE_PATH/vtn.key" ] \
    && [ "$(cat "$KEYSTORE_PATH/vtn.host" 2>/dev/null)" = "$VTN_HOST" ]; then
    return 0
  fi
  make_cert
  RENEWED=1
}

# vtn20b 가 떠 있는지
vtn_running() {
  [ -n "$(compose ps -q vtn20b 2>/dev/null)" ]
}

# VTN 이 뜰 때까지 기다렸다가(최대 3분) 실제로 내미는 서버 인증서의 이름(SAN)을 찍는다.
# VTN_HOST 가 이름에 없으면 알려 준다. VTN 이 안 떠도 start 는 실패로 끝내지 않는다(로그를 보라고만 한다)
check_cert() {
  echo
  echo "VTN 이 뜨기를 기다린다(최대 3분): ${MAIN_HOST}:${VTN_PORT}"
  i=0
  while [ "$i" -lt 60 ]; do
    # openssl 1.0 에는 x509 -ext 가 없어서 -text 에서 찾는다
    names=$(openssl s_client -connect "${MAIN_HOST}:${VTN_PORT}" </dev/null 2>/dev/null \
      | openssl x509 -noout -text 2>/dev/null | grep -A1 "Subject Alternative Name" | tail -1)
    if [ -n "$names" ]; then
      echo "VTN 서버 인증서 이름:$names"
      for host in $(echo "$VTN_HOST" | tr ',' ' '); do
        case "$names" in
          *"$host"*) ;;
          *) echo "주의: 인증서에 $host 가 없다. bin/run.sh cert 로 다시 만든다" ;;
        esac
      done
      return 0
    fi
    i=$((i + 1))
    sleep 3
  done
  echo "VTN 이 3분 안에 뜨지 않았다. bin/run.sh logs vtn20b 로 본다"
}

start() {
  require_images
  # 바인드 디렉토리를 미리 만든다(도커가 만들면 root 소유가 된다)
  mkdir -p "$PGDATA_PATH" "$LOG_PATH"
  running=0
  if vtn_running; then
    running=1
  fi
  ensure_cert
  # --renew-anon-volumes: rabbitmq, openfire 는 데이터 디렉토리를 익명 볼륨으로 잡는다.
  # 이어 붙이면 예전 큐 메시지가 남고 이미지 메이저 버전을 올리면 옛 데이터 때문에 못 뜬다. 매번 새로 만든다
  compose up -d --renew-anon-volumes
  # 인증서 파일만 바뀌면 compose 는 vtn20b 를 다시 만들지 않는다. 떠 있던 VTN 이 새 인증서를 읽게 다시 띄운다
  if [ "$RENEWED" = 1 ] && [ "$running" = 1 ]; then
    compose restart vtn20b
  fi
  compose ps
  echo
  echo "VTN 웹 UI        https://${MAIN_HOST}:${VTN_PORT}/testvtn/  (admin / admin)"
  echo "VEN 에 넣을 주소  https://${MAIN_HOST}:${VTN_PORT}/testvtn/OpenADR2/Simple/2.0b"
  echo "RabbitMQ    http://${ADMIN_BIND}:${RABBITMQ_ADMIN_PORT}  (admin / admin)"
  echo "Openfire    http://${ADMIN_BIND}:${OPENFIRE_ADMIN_PORT}"
  check_cert
}

stop() {
  compose down
}

load() {
  file="${1:-$IMAGES_FILE}"
  [ -f "$file" ] || { echo "이미지 묶음이 없다: $file (로컬에서 deploy/oadr/build.sh 로 만들어 옮긴다)"; exit 1; }
  echo "이미지 넣기: $file"
  gunzip -c "$file" | docker load
}

backup() {
  dir="$BACKUP_PATH/$(date +%Y%m%d%H%M%S)"
  mkdir -p "$dir"
  for db in $DATABASES; do
    echo "백업 $db -> $dir/$db.dump"
    compose exec -T postgres pg_dump -U postgres -Fc -d "$db" > "$dir/$db.dump"
  done
  echo "백업했다: $dir"
}

restore() {
  dir="${1:-}"
  [ -n "$dir" ] && [ -d "$dir" ] || { echo "백업 디렉토리를 준다: bin/run.sh restore $BACKUP_PATH/<시각>"; exit 1; }
  # DB 를 쓰는 쪽을 먼저 내린다
  compose stop vtn20b openfire
  for db in $DATABASES; do
    [ -f "$dir/$db.dump" ] || { echo "덤프가 없다: $dir/$db.dump"; exit 1; }
    echo "복원 $dir/$db.dump -> $db"
    compose exec -T postgres pg_restore -U postgres --clean --if-exists -d "$db" < "$dir/$db.dump"
  done
  compose up -d openfire vtn20b
  echo "복원했다: $dir"
}

renew_cert() {
  make_cert
  if vtn_running; then
    compose restart vtn20b
    check_cert
  fi
}

reset_db() {
  echo "DB 데이터를 비운다: $PGDATA_PATH"
  compose down
  # 데이터 파일은 컨테이너의 postgres 사용자 소유라 호스트에서 바로 못 지운다. postgres 이미지로 지운다
  docker run --rm --entrypoint sh -v "$PGDATA_PATH":/data openadr/postgres:latest -c 'rm -rf /data/* /data/.[!.]*'
  echo "비웠다. bin/run.sh start 로 다시 올리면 postgres 가 init 스크립트를 다시 돌린다"
}

case "$COMMAND" in
  start)    start ;;
  stop)     stop ;;
  restart)  stop; start ;;
  status)   compose ps ;;
  logs)     compose logs -f "$@" ;;
  load)     load "$@" ;;
  update)   load "$@"; start ;;
  backup)   backup ;;
  restore)  restore "$@" ;;
  reset-db) reset_db ;;
  cert)     renew_cert ;;
  *)        sed -n '2,17p' "$0"; exit 1 ;;
esac
