#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
# 저장소 루트. 이 스크립트(docker/)의 한 단계 위다. compose 의 빌드 컨텍스트도 여기다
ROOT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)

# 서버 빌드(그래들)와 Openfire 플러그인이 있는 곳
SERVER_DIR="${SERVER_DIR:-$ROOT_DIR/oadr-server}"

# 테스트 인증서. cert/generate_test_cert.sh 가 만든다. 도커 이미지는 저장소 루트 cert 를 COPY 한다
CERT_DIR="$ROOT_DIR/cert"

# 서버 모듈은 클라이언트 라이브러리(oadr-client/)를 좌표로 받는다(oadr-server/gradle/libs.versions.toml 의 openadr-client).
# 그래들이 oadr-client 디렉토리를 includeBuild 로 물고 들어가서 소스에서 바로 만든다(oadr-server/settings.gradle).
# 저장소가 나뉘면 CLIENT_DIR 로 client 체크아웃 경로를 주거나, client 에서 publishToMavenLocal 을 해 두면 된다.
CLIENT_DIR="${CLIENT_DIR:-$ROOT_DIR/oadr-client}"

# compose 파일은 서비스 디렉토리마다 하나씩 있다(docker-compose.yml). 인프라는 docker/ 바로 아래,
# 이 저장소에서 빌드하는 앱은 docker/service 아래에 OpenADR 역할로 나눠 둔다(server 는 VTN 쪽, client 는 VEN 쪽).
# 전부 -f 로 모아 한 프로젝트로 올리고, 어느 서비스를 올릴지는 명령에서 고른다.
# 각 파일 안의 경로(build, context)는 저장소 루트 기준이다. compose 는 -f 로 여러 파일을 주면
# 상대 경로를 전부 한 기준(프로젝트 디렉토리, 기본은 첫 파일의 디렉토리)으로 풀어서 --project-directory 로 루트를 준다
COMPOSE_FILES="
docker/postgres/docker-compose.yml
docker/rabbitmq/docker-compose.yml
docker/openfire/docker-compose.yml
docker/service/build/docker-compose.yml
docker/service/server/vtn20b/docker-compose.yml
docker/service/server/dummy-drprogram/docker-compose.yml
docker/service/client/dummy-ven20b/docker-compose.yml
"

INFRA_SERVICES="postgres rabbitmq openfire"

# 앱 서비스 이름은 compose 의 서비스 이름, docker/service/*/<name> 디렉토리와 같다.
# all 로 올릴 때의 기동 순서다. 서비스를 직접 나열하면 적은 순서대로 뜬다. 내릴 때는 반대.
ALL_SERVICES="vtn20b dummy-drprogram dummy-ven20b"

# compose 프로젝트명. 안 주면 첫 compose 파일의 디렉토리 이름(postgres)이 된다.
# 예전에는 인프라 전용(oadr-infra)과 앱(oadr-app) 두 스택이었고 포트가 겹쳐서 서로 내려야 했다. 지금은 하나다
PROJECT="oadr"

# postgres 데이터 볼륨. postgres/docker-compose.yml 에서 name 으로 고정해 둔 이름과 같아야 한다
PGDATA_VOLUME="oadr_pgdata"

# 사용법: docker/run.sh <command> [target]
#   command  start | stop | restart | logs | status | build | reset-db
#   target   infra                    postgres, rabbitmq, openfire 만. 앱은 IntelliJ 에서 띄운다
#            vtn20b,dummy-ven20b      앱을 쉼표로. 빌드해서 컨테이너로 올린다. 적은 순서대로 뜬다
#            all                      infra 와 모든 앱 (기본값)
#
# 인프라까지 올리는 기동(all, infra)은 DB 를 비우고 시작한다.
# 매번 같은 상태에서 출발하려는 것이다. 서비스 하나만 다시 올릴 때는 안 비운다.
# 설정 하나 고쳐서 vtn20b 만 재기동하는 경우에 데이터가 날아가면 곤란하기 때문이다.
# 비우지 않고 전체를 올리고 싶으면 KEEP_DB=1 을 준다.
COMMAND="${1:-start}"
# 두 번째 인자부터 전부 target 으로 본다. "a,b" 도 "a, b" 도 "a b" 도 같다
[ $# -gt 0 ] && shift
TARGET=$(echo "${*:-all}" | tr ',' ' ' | tr -s ' ' | sed 's/^ //; s/ $//')
[ -z "$TARGET" ] && TARGET="all"

cd "$ROOT_DIR"

# 모든 compose 파일을 붙여서 부른다
compose() {
  # shellcheck disable=SC2046
  set -- $(for f in $COMPOSE_FILES; do printf -- '-f %s ' "$f"; done) "$@"
  docker compose -p "$PROJECT" --project-directory "$ROOT_DIR" "$@"
}

usage() {
  echo "Usage: $0 {start|stop|restart|logs|status|build|reset-db} [infra|<service>[,<service>...]|all]"
  echo "  services: $ALL_SERVICES"
  echo "  reset-db: DB 를 비운다(postgres 볼륨 삭제). ddl-auto 가 update 라 그래야 스키마까지 새로 만들어진다"
  echo "  all 과 infra 로 기동하면 DB 를 비우고 시작한다. 남기려면 KEEP_DB=1"
  echo
  echo "  브라우저로 볼 것: VTN 웹 UI  https://localhost:9970/testvtn/  (admin / admin)"
  echo "                   RabbitMQ 관리 http://localhost:9971  (admin / admin)"
  echo "                   Openfire 관리 http://localhost:9972"
  echo "  포트는 9970 대다: 9970 VTN, 9971 RabbitMQ 관리, 9972 Openfire 관리, 9973 AMQP, 9974 XMPP, 9975 PostgreSQL, 9976~ 더미 VEN"
  exit 1
}

# target 을 "infra 여부" 와 "서비스 목록" 으로 푼다
WITH_INFRA=0
SERVICES=""
case "$TARGET" in
  infra) WITH_INFRA=1 ;;
  all)   WITH_INFRA=1; SERVICES="$ALL_SERVICES" ;;
  *)
    for name in $TARGET; do
      case " $ALL_SERVICES " in
        *" $name "*) SERVICES="$SERVICES $name" ;;
        *) echo "unknown service: $name"; usage ;;
      esac
    done
    ;;
esac

# oadr-server/settings.gradle 에서 빌드에 들어 있는 모듈 목록을 뽑는다(include '모듈' 줄, // 주석 줄은 뺀다)
active_modules() {
  sed -n "s|^[[:space:]]*include[[:space:]]*['\"]\([^'\"]*\)['\"].*|\1|p" "$SERVER_DIR/settings.gradle"
}

# 앱 이미지는 네 모듈의 jar 를 필요로 한다. 하나라도 빌드 밖이면 만들 수 없다.
missing_modules() {
  active=$(active_modules)
  missing=""
  for m in OpenADRServerVTN20b OpenADRServerVEN20b DummyVEN20b DummyDRProgram; do
    echo "$active" | grep -qx "$m" || missing="$missing $m"
  done
  echo "$missing"
}

# 메이븐을 찾는다. Openfire 플러그인만 메이븐으로 빌드한다(부모가 Openfire 의 plugins pom 이다).
# 자바 25 도 같이 찾는다. mvn 이 PATH 에 없으면 IntelliJ 번들을 쓴다.
# 앱 jar 는 그래들 wrapper(./gradlew)로 만들고, 그래들은 툴체인으로 자바 25 를 알아서 고른다
resolve_build_tools() {
  if [ -z "${JAVA_HOME:-}" ] && [ -x /usr/libexec/java_home ]; then
    JAVA_HOME=$(/usr/libexec/java_home -v 25 2>/dev/null || true)
    export JAVA_HOME
  fi
  [ -n "${JAVA_HOME:-}" ] || { echo "JAVA_HOME 을 못 찾았다. 자바 25 를 지정해라"; exit 1; }

  if [ -z "${MVN:-}" ]; then
    if command -v mvn >/dev/null 2>&1; then
      MVN=mvn
    elif [ -x "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" ]; then
      MVN="/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"
    else
      echo "mvn 을 못 찾았다. MVN 환경변수로 경로를 지정해라"; exit 1
    fi
  fi
  export MVN
  echo "JAVA_HOME=$JAVA_HOME"
  echo "MVN=$MVN"
}

# 인증서가 없으면 TLS 로 뜨지 못한다. 최초 1회만 만들면 된다
assert_cert() {
  if [ ! -f "$CERT_DIR/vtn.oadr.com-rsa.crt" ]; then
    echo "cert 디렉토리에 인증서가 없다. 먼저 ./cert/generate_test_cert.sh 를 돌려라"
    exit 1
  fi
}

# Openfire 이미지에 넣을 OpenADR 플러그인을 호스트에서 빌드한다(docker/openfire/Dockerfile 참고).
# 그래들 빌드에 없는 별도 메이븐 프로젝트라 -f 로 따로 부른다. Openfire 의존성은 처음 한 번만 ~/.m2 로 받는다
build_openfire_plugin() {
  resolve_build_tools
  echo "Building Openfire OpenADR plugin"
  "$MVN" -B -ntp -f "$SERVER_DIR/OpenfireOadrPlugin/pom.xml" clean package -DskipTests
}

start_infra() {
  assert_cert
  build_openfire_plugin
  # --renew-anon-volumes: rabbitmq, openfire 이미지는 데이터 디렉토리를 익명 볼륨으로 잡는다.
  # compose 는 컨테이너를 다시 만들어도 익명 볼륨을 이어 붙여서, 예전 실행의 큐 메시지가 남고
  # 이미지 메이저 버전을 올리면(rabbitmq 3 -> 4) 옛 데이터 때문에 뜨지 못한다. 매번 새로 만든다.
  # postgres 는 이름 있는 볼륨(oadr_pgdata)이라 영향이 없다(비우는 건 drop_db 가 한다)
  echo "Starting infra ($INFRA_SERVICES)"
  # shellcheck disable=SC2086
  compose up -d --build --renew-anon-volumes $INFRA_SERVICES
}

stop_all() {
  echo "Stopping all"
  compose down
}

# 모든 앱 이미지가 openadr_build 이미지에서 jar 를 꺼내 온다.
# 그래서 앱을 올리기 전에 이 이미지를 한 번 만들어야 한다.
build_images() {
  assert_cert

  # 이미지에 들어갈 jar 를 먼저 로컬에서 만든다(각 모듈 build/libs).
  #
  # 메이븐 때는 프로파일(external, frontend)을 꼭 줘야 했고 clean 도 빠지면 안 됐다.
  # 지금은 VTN jar 에 두 브로커 라이브러리와 PostgreSQL 드라이버가 늘 같이 들어가고
  # (어느 브로커를 쓸지는 스프링 프로파일로 고른다), React UI 도 기본으로 들어간다.
  # 그래들은 입력이 바뀐 태스크만 다시 돌리므로 clean 도 필요 없다.
  # client 는 옆에 있으면 includeBuild 로 같이 빌드된다. 없으면 ~/.m2 에 올려 둔 걸 쓴다
  if [ -f "$CLIENT_DIR/settings.gradle" ]; then
    echo "Using client build ($CLIENT_DIR)"
  else
    echo "oadr-client 디렉토리가 없다($CLIENT_DIR). ~/.m2 에 publishToMavenLocal 해 둔 클라이언트 라이브러리를 쓴다"
  fi

  echo "Building jars (gradle assemble)"
  "$SERVER_DIR/gradlew" -p "$SERVER_DIR" --console=plain -PopenadrClientDir="$CLIENT_DIR" assemble

  echo "Building openadr_build image"
  # service/build/docker-compose.yml 의 image 이름(openadr_build:latest)으로 태그가 붙는다
  compose build build
}

start_services() {
  [ -n "$SERVICES" ] || return 0

  missing=$(missing_modules)
  if [ -n "$missing" ]; then
    echo
    echo "앱은 건너뛴다. 아직 빌드에 들어오지 않은 모듈이 있다:$missing"
    echo "oadr-server/settings.gradle 에 해당 모듈의 include 가 있어야 앱 이미지를 만들 수 있다."
    echo "지금은 인프라만 뜬 상태다. VTN 은 IntelliJ 에서 VTN20bApplication 으로 띄우면 된다."
    SERVICES=""
    return 0
  fi

  build_images
  for name in $SERVICES; do
    echo "Starting $name"
    compose up -d --build "$name"
  done
}

# 올린 순서의 반대로 내린다
stop_services() {
  [ -n "$SERVICES" ] || return 0
  reversed=""
  for name in $SERVICES; do reversed="$name $reversed"; done
  for name in $reversed; do
    echo "Stopping $name"
    compose rm -sf "$name"
  done
}

status() {
  compose ps
}

# 순서는 항상 infra -> 앱. 앱이 DB 와 브로커를 보고 뜬다
start() {
  # 인프라까지 올리는 기동은 빈 DB 에서 시작한다.
  # 서비스 하나만 올릴 때(WITH_INFRA=0)는 건드리지 않는다
  if [ "$WITH_INFRA" = 1 ] && [ -z "${KEEP_DB:-}" ]; then
    drop_db
  fi
  [ "$WITH_INFRA" = 1 ] && start_infra
  start_services
  status
  echo
  # 실제로 띄운 것만 알려준다. 앱을 건너뛴 경우 SERVICES 는 비어 있다
  if [ "$WITH_INFRA" = 1 ]; then
    echo "RabbitMQ 관리   http://localhost:9971  (admin / admin)"
    echo "Openfire 관리   http://localhost:9972"
  fi
  case " $SERVICES " in
    *" vtn20b "*)       echo "VTN 웹 UI      https://localhost:9970/testvtn/  (admin / admin)" ;;
  esac
  case " $SERVICES " in
    *" dummy-ven20b "*) echo "Dummy VEN      https://localhost:9977  (https 다. 클라이언트 인증서가 필요하다)" ;;
  esac
}

# 내릴 때는 반대로. 앱이 DB 를 보고 있으니 앱부터
stop() {
  stop_services
  [ "$WITH_INFRA" = 1 ] && stop_all
  return 0
}

# DB 를 통째로 비운다.
#
# ddl-auto 가 update 라 vtn20b 를 다시 올려도 스키마와 데이터가 남는다.
# 그게 평소에는 좋지만, 엔티티에서 컬럼 타입을 좁히거나 컬럼을 뺐을 때는
# update 가 그런 변경을 반영하지 않으므로 옛 스키마가 그대로 남는다.
# 처음부터 다시 만들려면 데이터 디렉토리를 통째로 버려야 한다.
# 데이터는 oadr_pgdata 볼륨에 있으니 컨테이너를 지우고 볼륨까지 지운다.
# 볼륨이 비어 있으면 postgres 이미지가 다음 기동에서 init-*.sh 를 다시 돌려 준다.
drop_db() {
  echo "Dropping database (postgres volume)"
  # 앱과 postgres 가 볼륨을 붙들고 있으니 먼저 다 내린다
  # shellcheck disable=SC2086
  compose rm -sf $ALL_SERVICES postgres >/dev/null 2>&1 || true
  docker volume rm -f "$PGDATA_VOLUME" >/dev/null 2>&1 || true
}

reset_db() {
  drop_db
  echo "비웠다. './docker/run.sh start all' 로 다시 올려라"
}

logs() {
  if [ -n "$SERVICES" ]; then
    # shellcheck disable=SC2086
    compose logs -f $SERVICES
  else
    compose logs -f
  fi
}

case "$COMMAND" in
  start)   start ;;
  stop)    stop ;;
  restart) stop; start ;;
  logs)    logs ;;
  status)  status ;;
  build)   "$SERVER_DIR/gradlew" -p "$SERVER_DIR" --console=plain -PopenadrClientDir="$CLIENT_DIR" assemble ;;
  reset-db) reset_db ;;
  *)       usage ;;
esac
