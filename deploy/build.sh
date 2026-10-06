#!/bin/sh
# 개발 서버에 올릴 VTN 스택 이미지를 로컬에서 만들고 tar 하나로 묶는다(서버에는 도커만 있으면 된다).
# IntelliJ 의 그래들 build 와는 따로 돈다. 배포할 때 이걸 한 번 돌린다.
#
# 하는 일
#   1. 서버 jar 를 그래들로 만든다(avob-server, 각 모듈 build/libs)
#   2. Openfire 플러그인을 메이븐으로 만든다(avob-server/OpenfireOadrPlugin/target)
#   3. 이미지 4개를 만든다. Dockerfile 은 로컬 docker/ 의 것을 그대로 쓴다
#      openadr/postgres, openadr/rabbitmq, openadr/openfire, openadr/vtn20b (태그 latest)
#   4. deploy/avob/images/avob-images.tar.gz 로 묶는다
#
# 개발 서버가 linux/amd64 라 그 플랫폼으로 만든다. 맥(arm64)에서는 에뮬레이션이라 느리다. 서버가 arm 이면 PLATFORM 을 바꾼다.
# 인증서는 저장소 루트 cert 의 테스트 인증서가 이미지에 들어간다(없으면 cert/generate_test_cert.sh 를 먼저 돌린다).
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
# 저장소 루트. 이 스크립트(deploy)의 한 단계 위다. docker build 의 컨텍스트도 여기다
ROOT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
SERVER_DIR="$ROOT_DIR/avob-server"
CLIENT_DIR="$ROOT_DIR/avob-client"
# 묶음은 서버에 올리는 디렉토리(deploy/avob) 아래에 둔다. README 의 scp 경로와 deploy/avob/.gitignore 가 이 자리를 본다
IMAGES_DIR="$SCRIPT_DIR/avob/images"
PLATFORM="linux/amd64"
IMAGES="openadr/postgres:latest openadr/rabbitmq:latest openadr/openfire:latest openadr/vtn20b:latest"

cd "$ROOT_DIR"

# 인증서가 없으면 VTN 이 TLS 로 뜨지 못한다
if [ ! -f cert/vtn.oadr.com-rsa.crt ]; then
  echo "cert 디렉토리에 인증서가 없다. 먼저 ./cert/generate_test_cert.sh 를 돌려라"
  exit 1
fi

# 자바 25 와 메이븐을 찾는다(docker/run.sh 와 같은 방식). 메이븐은 Openfire 플러그인만 쓴다
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

echo "1. jar (gradle assemble)"
"$SERVER_DIR/gradlew" -p "$SERVER_DIR" --console=plain -PopenadrClientDir="$CLIENT_DIR" assemble

echo "2. Openfire 플러그인 (maven package)"
"$MVN" -B -ntp -f "$SERVER_DIR/OpenfireOadrPlugin/pom.xml" clean package -DskipTests

echo "3. 이미지 ($PLATFORM)"
# vtn20b Dockerfile 이 jar 를 이 중간 이미지에서 꺼내 간다(COPY --from=openadr_build:latest). 같은 플랫폼으로 먼저 만든다
docker build --platform "$PLATFORM" -f docker/service/build/Dockerfile -t openadr_build:latest .
docker build --platform "$PLATFORM" -f docker/postgres/Dockerfile -t openadr/postgres:latest .
docker build --platform "$PLATFORM" -f docker/rabbitmq/Dockerfile -t openadr/rabbitmq:latest .
docker build --platform "$PLATFORM" -f docker/openfire/Dockerfile -t openadr/openfire:latest .
# vtn20b 는 컨텍스트가 자기 디렉토리다(설정 파일을 COPY 한다)
docker build --platform "$PLATFORM" -t openadr/vtn20b:latest docker/service/server/vtn20b

echo "4. 묶기"
mkdir -p "$IMAGES_DIR"
# shellcheck disable=SC2086
docker save $IMAGES | gzip > "$IMAGES_DIR/avob-images.tar.gz"

echo
echo "만들었다: $IMAGES_DIR/avob-images.tar.gz"
echo "서버로 옮기는 방법은 deploy/avob/README.md 참고(처음 올릴 때, 다시 배포할 때)"
