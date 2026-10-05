# deploy/oadr

개발 서버에 VTN 2.0b 스택(postgres, rabbitmq, openfire, vtn20b)을 도커로 올린다.
외부 VEN 을 이 VTN 에 붙여 실제 이벤트 발령과 리포트를 테스트한다.

이미지는 로컬에서 만들어 tar 로 옮긴다. 서버에는 도커(compose 포함)만 있으면 되고 소스, JDK, 메이븐은 필요 없다.

## 파일

- build.sh: 로컬에서만 쓴다(IntelliJ 그래들 build 와 따로). jar, Openfire 플러그인, 이미지 4개를 만들고 images/oadr-images.tar.gz 로 묶는다
- bin/run.sh: 서버에서 쓴다. start, stop, restart, status, logs, load, update, backup, restore, reset-db, cert
- config/oadr.env: 서버마다 고치는 값. VTN 주소(VTN_HOST), VTN ID(VTN_ID, 기본 AVOB_OPEN_ADR), 리포트 등록 응답에 요청 싣기(REPORT_REQUEST_ON_REGISTER, 기본 true. 등록 응답의 요청으로만 리포트를 보내는 VEN 에 필요), 포트(VTN, 관리 화면), 인증서, DB 데이터, 백업, 로그 디렉토리, 시간대
- docker-compose.yml: 이미지로만 띄운다(build 없음). 값은 config/oadr.env 에서 받는다

Dockerfile 과 VTN 설정(application.properties)은 로컬 docker/ 의 것을 그대로 쓴다. 바꾸면 build.sh 부터 다시 한다.

## 서버 디렉토리

```
/svc/app/oadr            (어디든 된다)
  bin/run.sh
  config/oadr.env
  docker-compose.yml
  images/oadr-images.tar.gz
  keystore               KEYSTORE_DIR. bin/run.sh 가 만든 VTN 서버 인증서(vtn.key, vtn.crt)와 CA(ca.crt)
  data/postgres          PGDATA_DIR. 재기동, 재배포해도 남는다
  backup/<시각>/          BACKUP_DIR. bin/run.sh backup 결과
  logs/vtn20b            LOG_DIR. VTN 로그
```

config/oadr.env 의 경로가 상대 경로면 docker-compose.yml 이 있는 디렉토리 기준이다. 절대 경로도 된다.

## 처음 올릴 때

로컬(저장소 루트)

```
deploy/oadr/build.sh
ssh <서버> mkdir -p /svc/app/oadr/images
scp -r deploy/oadr/bin deploy/oadr/config deploy/oadr/docker-compose.yml <서버>:/svc/app/oadr/
scp deploy/oadr/images/oadr-images.tar.gz <서버>:/svc/app/oadr/images/
```

서버

```
cd /svc/app/oadr
vi config/oadr.env        (포트, 디렉토리. VTN_HOST 는 기본 auto 라 서버 IP 를 알아서 넣는다)
bin/run.sh update
```

## 다시 배포할 때

로컬에서 build.sh 를 다시 돌리고 이미지 묶음만 옮긴다. config/oadr.env 는 덮어쓰지 않는다.
bin/run.sh, docker-compose.yml 이 바뀌었으면 그것도 옮긴다.

```
scp deploy/oadr/images/oadr-images.tar.gz <서버>:/svc/app/oadr/images/
ssh <서버> /svc/app/oadr/bin/run.sh update
```

## DB 백업

- bin/run.sh backup: VTN(oadr-vtn20b), Openfire(oadr-openfire) DB 를 BACKUP_DIR/<시각>/ 에 pg_dump -Fc 로 남긴다. 스택이 떠 있어야 한다
- bin/run.sh restore BACKUP_DIR/<시각>: 그 덤프로 되돌린다. vtn20b, openfire 를 잠깐 내렸다 올린다
- 매일 남기려면 cron 에 건다: 0 3 * * * /svc/app/oadr/bin/run.sh backup
- 오래된 백업은 지우지 않는다. 필요하면 BACKUP_DIR 을 따로 정리한다

## VTN 서버 인증서

VEN 이 VTN 에 붙을 때 VTN 서버 인증서의 이름(SAN)과 접속 주소가 같아야 한다.
이미지 안 인증서에는 vtn.oadr.com, localhost, 127.0.0.1 만 있어서, 서버 주소로 붙으려면 그 주소가 들어간 인증서가 있어야 한다.

- bin/run.sh start(update)가 KEYSTORE_DIR 에 인증서가 없거나 VTN_HOST 가 바뀌었으면 VTN_HOST 로 인증서를 만든다.
  VTN_HOST=auto(기본)면 이 서버 IP(기본 경로의 src 주소)를 넣는다. 서버 IP 가 바뀌면 bin/run.sh start 를 한 번 돌린다.
  새 IP 로 인증서를 다시 만들고 떠 있던 vtn20b 를 다시 띄운다(재부팅으로 컨테이너만 다시 뜬 경우는 예전 인증서 그대로다)
  이미지 안 CA(oadr.com)로 서명한다. 서버에 openssl 이 있어야 한다
- vtn20b 는 KEYSTORE_DIR 을 마운트해서 이 인증서를 쓴다(환경 변수 OADR_SECURITY_VTN_KEY, CERT 가 이미지 설정을 덮는다)
- 다시 만들려면 bin/run.sh cert. 떠 있던 vtn20b 는 다시 띄운다
- CA 는 그대로라 관리자 화면에서 VEN 을 만들 때 받는 VEN 인증서, CA 와 짝이 맞는다. 이미지를 다른 PC 에서 새로 만들면 CA 가 바뀌니
  bin/run.sh cert 로 다시 만들고 VEN 인증서도 다시 받는다

## 확인

- bin/run.sh start(update, cert)는 끝에 VTN 이 뜨기를 기다렸다가(최대 3분) 실제로 내미는 서버 인증서 이름(SAN)을 찍는다. VTN_HOST 가 없으면 알려 준다
- VTN 웹 UI: https://<서버>:<VTN_PORT>/testvtn/ (admin / admin). 테스트 인증서라 브라우저가 경고를 낸다
- RabbitMQ, Openfire 관리 화면은 ADMIN_BIND(기본 127.0.0.1)에만 연다. 밖에서 보려면 ssh -L 9971:localhost:9971, ssh -L 9972:localhost:9972

## VEN 연동

- VEN 에 넣을 VTN 주소는 https://<VTN_HOST>:<VTN_PORT>/testvtn/OpenADR2/Simple/2.0b 다(bin/run.sh start 가 찍어 준다).
  VTN 관리 화면 VTN 설정 > 설정값의 VEN 연결 정보에 VTN ID, 2.0b 엔드포인트(화면에 들어온 주소로 만든다), MarketContext 가 있고 복사 버튼으로 옮긴다
  VTN_HOST 가 인증서에 들어 있어서 hosts 매핑이나 extra_hosts 가 따로 필요 없다
- VEN 인증서는 VTN 관리자 화면에서 VEN 을 만들고 받은 VEN crt, key, CA 를 VEN 쪽에 넣는다
- VEN 이 있는 곳에서 VTN_HOST:VTN_PORT 로 나가는 길만 열어 준다. pull 방식 VEN 이면 VTN 이 VEN 을 부르지 않는다
- 로컬(docker/)은 지금처럼 vtn.oadr.com 으로 붙는다(VEN 쪽 compose 의 extra_hosts 나 hosts 파일)

## 주의

- 이미지에 저장소 cert 의 테스트 인증서와 개인 키, 기본 계정(VTN admin / admin, RabbitMQ admin / admin, DB supersecure)이 들어 있다.
  개발 서버 전용이다. VTN_PORT 는 필요한 곳에만 연다
- 이미지는 linux/amd64 로 만든다(build.sh 의 PLATFORM). 서버가 arm 이면 바꾼다
- bin/run.sh reset-db 는 VTN, Openfire DB 를 통째로 지운다. 먼저 backup 을 남긴다
- docker-compose.yml 의 postgres 는 seccomp 를 끈다(security_opt: seccomp=unconfined). CentOS 7(커널 3.10)에서
  postgres:18-alpine 의 initdb 가 Operation not permitted 로 죽어서다. 커널이 새로운 서버면 지워도 된다
