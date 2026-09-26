#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)

# compose 파일은 이 스크립트 옆에 있다.
# infra 쪽은 postgres, rabbitmq, openfire 만, 기본 쪽은 인프라와 앱을 모두 정의한다.
# 두 파일 모두 빌드 컨텍스트가 리포지토리 루트라서 context 가 ".." 다.
INFRA_COMPOSE="docker/docker-compose.infra.yml"
APP_COMPOSE="docker/docker-compose.yml"

# 서비스 이름은 compose 의 서비스 이름, .docker/<name> 디렉토리와 전부 같다.
# all 로 올릴 때의 기동 순서다. 서비스를 직접 나열하면 적은 순서대로 뜬다. 내릴 때는 반대.
ALL_SERVICES="vtn20b dummy-drprogram dummy-ven20b"

# compose 프로젝트명. 기본값은 디렉토리 이름(OpenADR)이라 두 스택이 서로를 고아로 본다.
# 스택마다 따로 준다.
INFRA_PROJECT="oadr-infra"
APP_PROJECT="oadr-app"

# postgres 데이터 볼륨. 두 compose 파일에서 name 으로 고정해 둔 이름과 같아야 한다
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

usage() {
  echo "Usage: $0 {start|stop|restart|logs|status|build|reset-db} [infra|<service>[,<service>...]|all]"
  echo "  services: $ALL_SERVICES"
  echo "  reset-db: DB 를 비운다(postgres 볼륨 삭제). ddl-auto 가 update 라 그래야 스키마까지 새로 만들어진다"
  echo "  all 과 infra 로 기동하면 DB 를 비우고 시작한다. 남기려면 KEEP_DB=1"
  echo
  echo "  브라우저로 볼 것: VTN 웹 UI  https://localhost:8181/testvtn/  (admin / admin)"
  echo "                   RabbitMQ 관리 http://localhost:15672  (admin / admin)"
  echo "                   Openfire 관리 http://localhost:9090"
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

# 루트 pom 에서 실제로 활성인 모듈 목록을 뽑는다.
# 이관 안 된 모듈은 <modules> 안에 XML 주석으로 묶여 있다.
# 단순 grep 은 주석 안의 줄도 잡고, sed 의 범위 삭제는 한 줄짜리 주석에서
# 범위가 안 닫혀 멀쩡한 줄까지 지운다. 그래서 주석을 문자 단위로 걷어낸다.
active_modules() {
  awk '
    {
      line = $0; out = ""
      while (1) {
        if (inc) {
          p = index(line, "-->")
          if (p == 0) { line = ""; break }
          line = substr(line, p + 3); inc = 0
        } else {
          p = index(line, "<!--")
          if (p == 0) { out = out line; break }
          out = out substr(line, 1, p - 1)
          line = substr(line, p + 4); inc = 1
        }
      }
      print out
    }
  ' pom.xml | sed -n 's|.*<module>\./\([^<]*\)</module>.*|\1|p'
}

# 앱 이미지는 네 모듈의 jar 를 필요로 한다. 하나라도 리액터 밖이면 만들 수 없다.
missing_modules() {
  active=$(active_modules)
  missing=""
  for m in OpenADRServerVTN20b OpenADRServerVEN20b DummyVEN20b DummyDRProgram; do
    echo "$active" | grep -qx "$m" || missing="$missing $m"
  done
  echo "$missing"
}

# 자바 25 와 메이븐을 찾는다. mvn 이 PATH 에 없으면 IntelliJ 번들을 쓴다
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
  if [ ! -f "cert/vtn.oadr.com-rsa.crt" ]; then
    echo "cert 디렉토리에 인증서가 없다. 먼저 ./generate_test_cert.sh 를 돌려라"
    exit 1
  fi
}

# 인프라만 띄울 때와 앱까지 띄울 때는 쓰는 compose 파일이 다르다.
#
# docker-compose.yml 에는 인프라와 앱이 모두 들어 있다. 앱을 띄우면 compose 가
# 의존하는 인프라도 같은 프로젝트 안에 만든다. 그래서 앱을 띄울 때 인프라를
# 별도 프로젝트로 또 띄워 두면 5672 같은 포트를 서로 뺏는다.
#
# 앱까지 띄우는 경우에는 docker-compose.yml 하나만 쓰고,
# 앱은 IntelliJ 에서 돌리고 인프라만 필요한 경우에만 middleware 파일을 쓴다.
# Openfire 이미지에 넣을 OpenADR 플러그인을 호스트에서 빌드한다(docker/openfire/Dockerfile 참고).
# 루트 리액터에 없는 별도 프로젝트라 -f 로 따로 부른다. Openfire 의존성은 처음 한 번만 ~/.m2 로 받는다
build_openfire_plugin() {
  resolve_build_tools
  echo "Building Openfire OpenADR plugin"
  "$MVN" -B -ntp -f OpenfireOadrPlugin/pom.xml clean package -DskipTests
}

start_infra() {
  assert_cert
  build_openfire_plugin
  # --renew-anon-volumes: rabbitmq, openfire 이미지는 데이터 디렉토리를 익명 볼륨으로 잡는다.
  # compose 는 컨테이너를 다시 만들어도 익명 볼륨을 이어 붙여서, 예전 실행의 큐 메시지가 남고
  # 이미지 메이저 버전을 올리면(rabbitmq 3 -> 4) 옛 데이터 때문에 뜨지 못한다. 매번 새로 만든다.
  # postgres 는 이름 있는 볼륨(oadr_pgdata)이라 영향이 없다(비우는 건 drop_db 가 한다)
  if [ -n "$SERVICES" ]; then
    echo "Starting infra (postgres, rabbitmq, openfire)"
    docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" up -d --build --renew-anon-volumes postgres rabbitmq openfire
  else
    stop_app_stack_if_running
    echo "Starting infra (postgres, rabbitmq, openfire)"
    # 서비스를 직접 나열한다. compose 파일에 자바 빌드용 build 서비스도 들어 있는데
    # 그건 인프라만 띄울 때는 필요 없다
    docker compose -p "$INFRA_PROJECT" -f "$INFRA_COMPOSE" up -d --build --renew-anon-volumes postgres rabbitmq openfire
  fi
}

# 두 스택이 같은 포트를 쓰므로 한쪽을 띄울 때 다른 쪽은 내린다
stop_infra_stack_if_running() {
  if [ -n "$(docker compose -p "$INFRA_PROJECT" -f "$INFRA_COMPOSE" ps -q 2>/dev/null)" ]; then
    echo "인프라 전용 스택이 떠 있어 먼저 내린다 (포트가 겹친다)"
    docker compose -p "$INFRA_PROJECT" -f "$INFRA_COMPOSE" down
  fi
}

stop_app_stack_if_running() {
  if [ -n "$(docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" ps -q 2>/dev/null)" ]; then
    echo "앱 스택이 떠 있어 먼저 내린다 (포트가 겹친다)"
    docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" down
  fi
}

stop_infra() {
  echo "Stopping infra"
  docker compose -p "$INFRA_PROJECT" -f "$INFRA_COMPOSE" down 2>/dev/null || true
  docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" down 2>/dev/null || true
}

# 모든 앱 이미지가 openadr_build 이미지에서 jar 를 꺼내 온다.
# 그래서 앱을 올리기 전에 이 이미지를 한 번 만들어야 한다.
build_images() {
  assert_cert
  resolve_build_tools

  # 이미지에 들어갈 jar 를 먼저 로컬에서 만든다.
  #
  # Dockerfile-build 는 jar 가 이미 있으면 컨테이너 안에서 다시 빌드하지 않는다.
  # 그래서 여기서 만든 jar 가 그대로 이미지로 들어간다.
  # 프로파일이 중요하다. 스택의 브로커가 rabbitmq 컨테이너라 external 이어야 하고,
  # 기본값인 standalone 으로 만들면 rabbitmq 드라이버가 빠져서
  # VTN 이 RMQConnectionFactory 를 못 찾고 죽는다.
  # frontend 는 React UI 를 jar 안에 넣는 프로파일이다.
  #
  # clean 이 꼭 있어야 한다. 프로파일은 의존성만 바꾸고 소스는 그대로라서,
  # 직전에 IntelliJ 나 mvn install 로 standalone jar 가 만들어져 있으면
  # 메이븐이 jar 를 최신이라고 보고 다시 묶지 않는다. 그러면 standalone jar 가
  # 그대로 이미지에 들어가서 위에 적은 대로 VTN 이 기동 중에 죽는다.
  # pom 만 고쳤을 때 이미지에 반영이 안 되던 것도 같은 이유다
  echo "Building jars (profile: external, frontend)"
  "$MVN" -B clean package -P external,frontend -DskipTests
  # VTN 의 loader.path 로 들어갈 PostgreSQL 드라이버를 루트 target 에 복사한다.
  # 예전에는 openadr_build 이미지 안에서 돌렸다. -N 은 루트 pom 에서만 돌린다는 뜻이다
  "$MVN" -B -N dependency:copy@copy-external-dependency

  echo "Building openadr_build image"
  docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" build build
  docker image tag "${APP_PROJECT}-build" openadr_build:latest
}

start_services() {
  [ -n "$SERVICES" ] || return 0

  missing=$(missing_modules)
  if [ -n "$missing" ]; then
    echo
    echo "앱은 건너뛴다. 아직 리액터에 들어오지 않은 모듈이 있다:$missing"
    echo "루트 pom.xml 의 <modules> 에서 해당 항목의 주석을 풀어야 앱 이미지를 만들 수 있다."
    echo "지금은 인프라만 뜬 상태다. VTN 은 IntelliJ 에서 VTN20aApplication 으로 띄우면 된다."
    SERVICES=""
    return 0
  fi

  build_images
  for name in $SERVICES; do
    echo "Starting $name"
    docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" up -d --build "$name"
  done
}

# 올린 순서의 반대로 내린다
stop_services() {
  [ -n "$SERVICES" ] || return 0
  reversed=""
  for name in $SERVICES; do reversed="$name $reversed"; done
  for name in $reversed; do
    echo "Stopping $name"
    docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" rm -sf "$name"
  done
}

status() {
  echo "Local stack"
  docker compose -p "$INFRA_PROJECT" -f "$INFRA_COMPOSE" ps
  docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" ps
}

# 순서는 항상 infra -> 앱. 앱이 DB 와 브로커를 보고 뜬다
start() {
  [ -n "$SERVICES" ] && stop_infra_stack_if_running
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
    echo "RabbitMQ 관리   http://localhost:15672  (admin / admin)"
    echo "Openfire 관리   http://localhost:9090"
  fi
  case " $SERVICES " in
    *" vtn20b "*)       echo "VTN 웹 UI      https://localhost:8181/testvtn/  (admin / admin)" ;;
  esac
  case " $SERVICES " in
    *" dummy-ven20b "*) echo "Dummy VEN      https://localhost:8083  (https 다. 클라이언트 인증서가 필요하다)" ;;
  esac
}

# 내릴 때는 반대로. 앱이 DB 를 보고 있으니 앱부터
stop() {
  stop_services
  [ "$WITH_INFRA" = 1 ] && stop_infra
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
  for name in $ALL_SERVICES; do
    docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" rm -sf "$name" >/dev/null 2>&1 || true
  done
  docker compose -p "$INFRA_PROJECT" -f "$INFRA_COMPOSE" rm -sf postgres >/dev/null 2>&1 || true
  docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" rm -sf postgres >/dev/null 2>&1 || true
  docker volume rm -f "$PGDATA_VOLUME" >/dev/null 2>&1 || true
}

reset_db() {
  drop_db
  echo "비웠다. './docker/run.sh start all' 로 다시 올려라"
}

logs() {
  if [ -n "$SERVICES" ]; then
    # shellcheck disable=SC2086
    docker compose -p "$APP_PROJECT" -f "$APP_COMPOSE" logs -f $SERVICES
  else
    docker compose -p "$INFRA_PROJECT" -f "$INFRA_COMPOSE" logs -f
  fi
}

case "$COMMAND" in
  start)   start ;;
  stop)    stop ;;
  restart) stop; start ;;
  logs)    logs ;;
  status)  status ;;
  build)   resolve_build_tools; "$MVN" -B clean install -DskipTests ;;
  reset-db) reset_db ;;
  *)       usage ;;
esac
