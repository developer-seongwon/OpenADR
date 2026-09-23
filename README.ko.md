# OpenADR 실행 가이드 (한글)

Spring Boot 4 / Java 25 로 이관한 뒤의 빌드와 실행 방법이다.
프로토콜 자체와 모듈 설명은 영문 `README.md` 를 보면 된다.

## 준비물

Docker Desktop, Java 25, Maven 이 필요하다.
Node 는 따로 깔 필요 없다. 프론트엔드 빌드에 쓰는 Node 16 은 메이븐이 알아서 받아 쓴다.

pom 의 `java.version` 이 25 라서 25 미만 JDK 로는 컴파일 자체가 안 된다.

```
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
```

맥에서 `mvn` 이 PATH 에 없으면 IntelliJ 번들 메이븐을 쓰면 된다.

```
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -B clean install
```

`docker/run.sh` 는 `JAVA_HOME` 과 `mvn` 을 알아서 찾는다. 못 찾으면 에러를 내고 멈춘다.
직접 지정하고 싶으면 `JAVA_HOME`, `MVN` 환경변수를 넘기면 된다.

## 최초 1회: 테스트 인증서 생성

OpenADR 은 VTN 과 VEN 이 서로 인증서로 신원을 확인한다.
`cert/` 가 비어 있으면 처음 한 번은 직접 만들어야 한다.

```
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

jar 를 먼저 로컬에서 빌드한 다음(`-P external,frontend`) 이미지를 만들고 컨테이너를 띄운다.
처음에는 이미지 빌드까지 포함해서 몇 분 걸린다.

뜨고 나면 이렇게 접속한다.

VTN 웹 UI 는 https://localhost:8181/testvtn/ 이고 `admin` / `admin` 으로 로그인한다.
자체 서명 인증서라 브라우저가 경고를 낸다. 그냥 진행하면 된다.
클라이언트 인증서는 필요 없다. `cert/admin.oadr.com.p12` (비밀번호 changeme) 를 브라우저에
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

command 는 `start`, `stop`, `restart`, `logs`, `status`, `build` 중 하나다.

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

인프라 전용 스택과 앱 스택은 포트가 겹쳐서 동시에 뜨지 못한다.
한쪽을 띄우면 다른 쪽은 스크립트가 알아서 내린다.

## docker 디렉토리 구조

```
docker/
  run.sh                     기동 스크립트. 여기만 쓰면 된다
  docker-compose.yml         인프라 + 앱 전체
  docker-compose.infra.yml   인프라만 (postgres, rabbitmq, openfire)
  build/                     자바 빌드 이미지. 여기서 만든 jar 를 앱 이미지들이 꺼내 간다
  postgres/                  DB. 초기화 스크립트가 oadr-vtn20b 와 openfire 스키마를 만든다
  rabbitmq/                  메시지 브로커
  openfire/                  XMPP 서버. VEN 이 xmpp 로 붙을 때 쓴다
  vtn20b/                    VTN 2.0b 서버 (웹 UI 포함)
  dummy-drprogram/           DR 프로그램 흉내. 주기적으로 이벤트를 만든다
  dummy-ven20b/              VEN 4대 흉내. http, simpleHttp, xmpp 로 각각 붙는다
```

각 디렉토리에 그 서비스의 `Dockerfile` 과 설정 파일이 같이 있다.

빌드 컨텍스트가 두 종류다. `build`, `postgres`, `rabbitmq`, `openfire` 는 리포지토리
루트를 컨텍스트로 쓴다. 소스와 `cert/` 가 필요해서다. 그래서 compose 의 context 가 `..` 이고
루트의 `.dockerignore` 가 적용된다. 나머지 앱 이미지는 자기 디렉토리만 컨텍스트로 쓴다.

## 동작 방식

앱 이미지 세 개는 전부 `openadr_build` 이미지에서 jar 를 꺼내 온다.
그래서 앱을 띄우기 전에 그 이미지가 먼저 만들어져야 하고, `run.sh` 가 그 순서를 지킨다.

jar 는 컨테이너 안이 아니라 로컬에서 만든다. 프로파일이 중요하다.
브로커가 rabbitmq 컨테이너라 `external` 이어야 하고, 기본값인 `standalone` 으로 만들면
rabbitmq 드라이버가 빠져서 VTN 이 `RMQConnectionFactory` 를 못 찾고 죽는다.
`frontend` 는 React UI 를 jar 안에 넣는 프로파일이다.

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

빌드가 `invalid target release: 25` 로 깨지면 25 미만 JDK 로 돌린 것이다.
`JAVA_HOME` 을 확인해라.

VEN20b 테스트는 실제로 서버 소켓을 연다. 포트는 18081, 18082 를 쓴다.
`OpenADRServerVEN20b/src/test/resources/application.properties` 에 있고,
`OadrMockMvc` 가 요청 URL 에 포트를 박아 쓰므로 바꾸려면 양쪽을 같이 고쳐야 한다.

테스트는 도커로 PostgreSQL 을 띄운다. 도커가 꺼져 있으면 컨텍스트가 못 뜬다.
컨테이너는 JVM 당 하나를 재사용하고, 그 홀더는 `OpenADRServerVTNTestSupport`
모듈에 있다. 테스트만 쓰는 모듈이라 운영 산출물에는 안 들어간다.

`mvn test`, `mvn install`, `-P external` 어느 조합으로 돌려도 똑같이 통과한다.

SPA 라우트로 바로 들어가거나 새로고침해도 404 가 나지 않아야 한다.
`/ven`, `/event/detail/...` 같은 경로는 `SpaIndexController` 가 index.html 을 내준다.
새 프론트 라우트를 추가하면 그 컨트롤러와 `HttpSecurityConfig` 양쪽에 경로를 같이 넣어야 한다.
