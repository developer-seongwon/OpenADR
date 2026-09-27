# OpenADR 실행 가이드 (한글)

Spring Boot 4 / Java 25 로 이관한 뒤의 빌드와 실행 방법이다.
프로토콜 자체와 모듈 설명은 영문 `README.md` 를 보면 된다.

## 디렉토리 구조

클라이언트 라이브러리와 서버를 디렉토리로 나눴다. 나중에 저장소를 나누면 각 디렉토리가 저장소 루트가 된다.

```
settings.gradle  client, server 를 한 번에 열고 빌드하는 묶음(composite build). 설정은 물려주지 않는다
build.gradle     묶음 태스크(build, assemble, test, check, clean, publishToMavenLocal, testReport)
client/          라이브러리. server 를 참조하지 않는다
  OpenADRSecurity, OpenADRModel20a, OpenADRModel20b,
  OpenADRHTTPClient, OpenADRHTTPClient20a, OpenADRHTTPClient20b, OpenADRXMPPClient
server/          서버와 테스트용 앱. client 를 좌표(com.avob.openadr:OpenADR*)로 받는다
  OpenADRServerVTNCommon, OpenADRServerVTNTestSupport, OpenADRServerVTN20a, OpenADRServerVTN20b,
  OpenADRServerVEN20b, DummyVEN20b, DummyDRProgram, OpenfireOadrPlugin(메이븐)
  test/http/, generate_test_cert.sh, cert/(생성물)
docker/          로컬 도커 스택(run.sh, compose, 서비스별 Dockerfile). server 의 jar 와 인증서를 쓴다
```

빌드는 그래들(wrapper 9.7.1)이다. client 와 server 가 각자 독립된 그래들 빌드라서
`settings.gradle`, `build.gradle`, `gradle/libs.versions.toml`(버전 목록), `gradlew` 를 따로 갖고 있다.
자바 버전, 테스트, BOM 정책 같은 공통 설정은 두 `build.gradle` 에 같게 들어 있으니 한쪽을 고치면 다른 쪽도 본다.
라이브러리 버전은 `gradle/libs.versions.toml` 에서 바꾼다. 버전이 안 적힌 것은 Spring Boot BOM 값을 그대로 쓴다.

OpenfireOadrPlugin 만 메이븐으로 남았다. 부모가 Openfire 의 plugins pom 이라 그래들로 옮길 이유가 없다.
`docker/run.sh` 가 따로 빌드한다.

## 준비물

Docker Desktop, Java 25 가 필요하다. Openfire 플러그인을 빌드할 때만 Maven 이 필요하다(`docker/run.sh` 가 씀).
그래들은 wrapper 가 알아서 받고, Node 도 따로 깔 필요 없다. 프론트엔드 빌드에 쓰는 Node 24 는 그래들이 받아 쓴다.

자바 버전은 그래들 툴체인(25)으로 고른다. 그래들 자체는 17 이상 아무 JDK 로 돌아도 되고,
컴파일과 테스트는 설치된 JDK 25 를 찾아서 쓴다. 없으면 받아 온다(foojay).

저장소 루트에서 client, server 를 한 번에 돌린다.

```
./gradlew build                  # client, server 빌드와 테스트
./gradlew assemble               # 테스트 없이 jar 만(루트에서는 -x test 로 포함된 빌드의 테스트를 못 뺀다)
./gradlew test                   # 전체 테스트. 끝나면 모듈별 건수를 찍고 build/reports/tests/index.html 에 합친 리포트를 만든다
./gradlew test --continue        # 한 모듈이 실패해도 나머지 모듈 테스트를 끝까지 돌린다
./gradlew clean
./gradlew publishToMavenLocal    # client 라이브러리를 ~/.m2 에 올린다
./gradlew :server:OpenADRServerVTN20b:bootRun   # 모듈 하나는 경로로 부른다
```

client, server 디렉토리에서 각각 돌려도 된다(저장소를 나눈 뒤에는 이렇게 쓴다).
각 빌드의 루트에도 같은 이름의 묶음 태스크가 있어서 모듈 전체가 돈다.

```
cd client && ./gradlew build                  # 라이브러리 빌드와 테스트
cd client && ./gradlew publishToMavenLocal    # ~/.m2 에 올리기(server 를 client 없이 빌드할 때, kpx-service 용 jar)
cd server && ./gradlew build                  # 서버 빌드와 테스트(옆의 client 를 소스로 같이 빌드한다)
cd server && ./gradlew build -x test          # 테스트 없이 jar 만
cd server && ./gradlew build -Pfrontend=false # VTN20b 에 React UI 를 넣지 않는다(node 빌드를 건너뛰어 빠르다)
```

server 는 옆에 client 디렉토리가 있으면 includeBuild 로 물고 들어가서 client 소스에서 바로 만들어 쓴다.
그래서 client 를 고친 뒤 따로 install 할 필요가 없다. 저장소를 나눈 뒤에는 `-PopenadrClientDir=<client 경로>` 로
위치를 주거나, client 에서 `publishToMavenLocal` 을 해 두면 server 가 ~/.m2 에서 받는다.

jar 는 각 모듈의 `build/libs` 에 생긴다. VTN20a, VTN20b, DummyVEN20b, DummyDRProgram 은 스프링 부트 실행 jar 하나만 만든다.
kpx-service 에 넣는 jar 는 `client/OpenADRModel20b/build/libs`, `client/OpenADRSecurity/build/libs` 에 있다.

IntelliJ 에서는 저장소 루트를 열면 루트의 `settings.gradle` 이 client 와 server 를 같이 불러온다.
Gradle 창의 루트(OpenADR) 아래 Tasks 에 묶음 태스크가 있고, 모듈별 태스크는 OpenADRClient, OpenADRServer 아래에 있다.

`docker/run.sh` 는 앱 jar 를 `./gradlew` 로 만들고, Openfire 플러그인용 `mvn` 은 알아서 찾는다.
못 찾으면 에러를 내고 멈춘다. 직접 지정하고 싶으면 `MVN` 환경변수를 넘기면 된다.

## 최초 1회: 테스트 인증서 생성

OpenADR 은 VTN 과 VEN 이 서로 인증서로 신원을 확인한다.
`server/cert/` 가 비어 있으면 처음 한 번은 직접 만들어야 한다.

```
cd server
./generate_test_cert.sh
```

CA, VTN, VEN, 관리자 인증서가 생긴다.
이게 없으면 `docker/run.sh` 가 먼저 만들라고 알려주고 멈춘다.

## 최초 1회: hosts 등록

VTN 인증서가 `vtn.oadr.com` 이름으로 발급되고, VEN20b 의 XMPP 테스트도
그 이름으로 붙는다. hosts 에 없으면 퍼블릭 DNS 로 나갔다가 타임아웃이 나서
테스트가 깨진다.

```
echo "127.0.0.1 vtn.oadr.com" | sudo tee -a /etc/hosts
```

## 전체 스택 띄우기

```
./docker/run.sh start all
```

이 문서의 `./docker/run.sh` 명령은 모두 저장소 루트 기준이다. 스크립트는 어디서 불러도 저장소 루트로 이동해서 돈다.
서버 빌드는 `server/gradlew`, 인증서는 `server/cert` 를 쓴다(`SERVER_DIR` 로 server 위치를 바꿀 수 있다).

jar 를 먼저 로컬에서 빌드한 다음(`./gradlew assemble`) 이미지를 만들고 컨테이너를 띄운다.
옆에 `client` 디렉토리가 있으면 그걸 같이 빌드한다. 저장소가 나뉘면 `CLIENT_DIR` 로 client 경로를 주거나
client 에서 `publishToMavenLocal` 을 미리 해 두면 된다.
처음에는 이미지 빌드까지 포함해서 몇 분 걸린다.

뜨고 나면 이렇게 접속한다.

VTN 웹 UI 는 https://localhost:8181/testvtn/ 이고 `admin` / `admin` 으로 로그인한다.
자체 서명 인증서라 브라우저가 경고를 낸다. 그냥 진행하면 된다.
클라이언트 인증서는 필요 없다. `server/cert/admin.oadr.com.p12` (비밀번호 changeme) 를 브라우저에
넣으면 x509 로도 들어갈 수 있는데, 로그인만 할 거면 안 해도 된다.

API 문서는 https://localhost:8181/testvtn/swagger-ui/index.html 이고,
스키마 자체는 https://localhost:8181/testvtn/v3/api-docs 다. 둘 다 로그인 없이 열린다.

RabbitMQ 관리 화면은 http://localhost:15672 이고 `admin` / `admin`.
Openfire 관리 화면은 http://localhost:9090.

Dummy VEN 은 https://localhost:8083 인데 https 이고 클라이언트 인증서를 요구한다.
브라우저로 볼 일은 거의 없다.

## run.sh 사용법

```
./docker/run.sh <command> [target]
```

command 는 `start`, `stop`, `restart`, `logs`, `status`, `build`, `reset-db` 중 하나다.
`build` 는 jar 만 만든다(`./gradlew assemble`).

target 은 세 가지로 준다.
`infra` 는 postgres, rabbitmq, openfire 만 띄운다. 앱은 IntelliJ 에서 돌릴 때 쓴다.
`all` 은 인프라와 앱을 전부 띄운다. 생략하면 이게 기본값이다.
서비스 이름을 직접 적을 수도 있다. `vtn20b`, `dummy-drprogram`, `dummy-ven20b` 이고
쉼표나 공백으로 여러 개를 나열하면 적은 순서대로 뜬다.

```
./docker/run.sh start infra
./docker/run.sh restart vtn20b
./docker/run.sh logs vtn20b,dummy-ven20b
./docker/run.sh status
./docker/run.sh stop all
```

인프라와 앱은 한 스택(compose 프로젝트 oadr)이다. `start infra` 로 인프라만 올려 두고
나중에 `start vtn20b` 처럼 앱만 더 올려도 된다.
인프라만 띄우고 VTN 을 IntelliJ 에서 돌려도 Openfire 가 VTN 에 닿는다. Openfire 는 vtn.oadr.com 을
호스트로 보내서(host-gateway), VTN 이 컨테이너면 호스트의 8181 포트 매핑을, IntelliJ 면 호스트의 VTN 을 만난다.

## docker 디렉토리 구조

```
docker/
  run.sh                       기동 스크립트. 여기만 쓰면 된다
  postgres/                    DB. 초기화 스크립트가 oadr-vtn20b 와 openfire 스키마를 만든다
  rabbitmq/                    메시지 브로커
  openfire/                    XMPP 서버. VEN 이 xmpp 로 붙을 때 쓴다
  service/                     이 저장소에서 빌드하는 앱
    build/                     jar 를 담는 중간 이미지(openadr_build). 앱 이미지들이 여기서 jar 를 꺼내 간다
    server/                    OpenADR 의 VTN 쪽
      vtn20b/                  VTN 2.0b 서버 (웹 UI 포함)
      dummy-drprogram/         DR 프로그램 흉내(VTN 뒤 운영 시스템 자리). 이벤트를 만들고 리포트를 받는다
    client/                    OpenADR 의 VEN 쪽
      dummy-ven20b/            VEN 흉내. http, simpleHttp, xmpp 로 각각 붙는다
```

각 디렉토리에 그 서비스의 `Dockerfile`, `docker-compose.yml`, 설정 파일이 같이 있다.
`run.sh` 가 이 compose 파일들을 전부 `-f` 로 모아 한 프로젝트(oadr)로 올리고, 어느 서비스를 올릴지만 명령에서 고른다.
새 서비스를 넣으면 디렉토리를 만들고 `run.sh` 의 `COMPOSE_FILES` 에 한 줄 더한다.

compose 파일 안의 경로는 전부 저장소 루트 기준이다. compose 는 `-f` 여러 개의 상대 경로를
한 기준 디렉토리로 풀어서 `run.sh` 가 `--project-directory` 로 저장소 루트를 준다.
`service/build`, `postgres`, `rabbitmq`, `openfire` 는 저장소 루트를 빌드 컨텍스트로 쓴다.
빌드한 jar(`server/*/build/libs`)와 `server/cert/` 가 필요해서다. 루트의 `.dockerignore`(COPY 하는 것만 들이는 허용 목록)가 적용된다.
나머지 앱 이미지는 자기 디렉토리만 컨텍스트로 쓴다.

## 동작 방식

앱 이미지 세 개는 전부 `openadr_build` 이미지에서 jar 를 꺼내 온다.
그래서 앱을 띄우기 전에 그 이미지가 먼저 만들어져야 하고, `run.sh` 가 그 순서를 지킨다.

jar 는 컨테이너 안이 아니라 로컬에서 그래들로 만든다.
VTN20b jar 에는 브로커 라이브러리 두 벌(ActiveMQ 내장 브로커, RabbitMQ JMS)과 PostgreSQL 드라이버가 늘 같이 들어간다.
어느 브로커를 쓸지는 스프링 프로파일로만 고른다. `standalone` 이면 내장 ActiveMQ, `external` 이면 RabbitMQ 다.
메이븐 때는 메이븐 프로파일로 둘 중 하나만 넣어서, 도커용 빌드에 `-P external` 을 빼먹으면 VTN 이
`RMQConnectionFactory` 를 못 찾고 죽었다. 그 함정은 없어졌다.
DummyDRProgram 도 ActiveMQ, RabbitMQ 클라이언트를 둘 다 넣고 스프링 프로파일로 고른다.

프론트엔드는 `server/OpenADRServerVTN20b/frontend` 에 있고 Vite 로 빌드한다(예전엔 react-scripts).
`npm run build` 결과가 `frontend/build` 에 생기고, 그래들이 그걸 jar 의 `public/` 에 바로 넣는다
(`frontendBuild` 태스크, 입력이 안 바뀌면 다시 돌지 않는다).
메이븐 때 복사해 두던 `src/main/resources/public` 은 이제 안 쓰고, 남아 있어도 jar 에서 빠진다.
IntelliJ 에서 VTN 을 띄울 때도 그래들로 실행하면(기본값) UI 가 같이 뜬다.
번들은 `static/` 아래에 둔다. `HttpSecurityConfig` 가 인증 없이 여는 경로가 `/static/**` 라서다.
JSX 가 든 파일은 확장자가 `.jsx` 여야 한다. Vite 는 `.js` 안의 JSX 를 읽지 않는다.

화면은 React 19, MUI 9, react-router 8 이다. 원래 MUI 3 으로 그려진 화면이라
`src/theme.js` 가 MUI 3 시절 기본값(색, 입력창 모양, Grid 폭, 탭 폭, 표 글씨)을 되살린다.
화면 모양이 이상하면 거기부터 보면 된다. 컴포넌트 스타일은 `tss-react` 의 `withStyles` 로 입힌다.

VTN 은 `fake-data,rabbitmq-broker,external` 프로파일로 뜬다.
`fake-data` 가 마켓 컨텍스트와 초기 계정을 심는다.

## 자주 걸리는 것들

VEN 목록이 비어 있으면 dummy 들이 아직 등록되기 전이다.
다시 올리면 1분에서 2분 안에 재등록된다.

```
./docker/run.sh restart dummy-drprogram,dummy-ven20b
```

## DB 가 언제 비워지나

인프라까지 올리는 기동은 DB 를 비우고 시작한다. 매번 같은 상태에서 출발하려는 것이다.

```
./docker/run.sh start all        # 비우고 시작
./docker/run.sh restart all      # 비우고 시작
./docker/run.sh start infra      # 비우고 시작
```

서비스 하나만 다시 올릴 때는 안 비운다. 설정 하나 고쳐서 vtn20b 만
재기동하는데 데이터가 날아가면 곤란하기 때문이다.

```
./docker/run.sh restart vtn20b   # DB 는 그대로
```

전체를 올리면서 DB 는 남기고 싶으면 `KEEP_DB=1` 을 준다.

```
KEEP_DB=1 ./docker/run.sh restart all
```

DB 만 따로 비우려면 이렇게 한다.

```
./docker/run.sh reset-db
```

비우는 방식은 postgres 데이터 볼륨(`oadr_pgdata`)을 지우는 것이다.
데이터가 볼륨에 있으니 컨테이너를 새로 만들어도 그대로 남고, 비우라고 할 때만 없어진다.
`ddl-auto` 는 `update` 라 스키마가 남는 쪽인데, 볼륨을 비우면 그것까지 새로 만들어진다.
엔티티에서 컬럼 타입을 좁히거나 컬럼을 뺀 변경도 이때 반영된다.
`docker/postgres/init-*.sh` 도 빈 볼륨에서만 도니까 같이 다시 돈다.

포트가 이미 쓰이고 있다고 나오면 예전 스택이 남아 있는 것이다.

```
./docker/run.sh stop all
```

8083 이 `ERR_CONNECTION_RESET` 을 내는 건 정상이다. http 가 아니라 https 이고
클라이언트 인증서를 요구한다.

그래들이 자바 25 툴체인을 못 찾는다고 하면 JDK 25 를 설치하거나 네트워크(foojay 로 받는다)를 확인해라.

VEN20b 테스트는 실제로 서버 소켓을 연다. 포트는 18081, 18082 를 쓴다.
`server/OpenADRServerVEN20b/src/test/resources/application.properties` 에 있고,
`OadrMockMvc` 가 요청 URL 에 포트를 박아 쓰므로 바꾸려면 양쪽을 같이 고쳐야 한다.

테스트는 도커로 PostgreSQL 을 띄운다. 도커가 꺼져 있으면 컨텍스트가 못 뜬다.
컨테이너는 JVM 당 하나를 재사용하고, 그 홀더는 `OpenADRServerVTNTestSupport`
모듈에 있다. 테스트만 쓰는 모듈이라 운영 산출물에는 안 들어간다.

VTNCommon, VTN20a, VTN20b 테스트는 모두 VTN 을 8182 포트로 띄워서, 그래들이 이 모듈들의 테스트는
하나씩 차례로 돌린다(server/build.gradle 의 serialTests). 컴파일은 병렬이다.

SPA 라우트로 바로 들어가거나 새로고침해도 404 가 나지 않아야 한다.
`/ven`, `/event/detail/...` 같은 경로는 `SpaIndexController` 가 index.html 을 내준다.
새 프론트 라우트를 추가하면 그 컨트롤러와 `HttpSecurityConfig` 양쪽에 경로를 같이 넣어야 한다.
