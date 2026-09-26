# 13 -> 18. 18 이미지부터 데이터 디렉토리가 /var/lib/postgresql/18/docker 로 바뀌었다.
# 그래서 compose 의 볼륨도 /var/lib/postgresql/data 가 아니라 /var/lib/postgresql 에 붙인다.
# 13 으로 만든 볼륨은 18 이 읽지 못한다(메이저 업그레이드는 pg_upgrade 가 필요하다).
# run.sh start all 은 어차피 볼륨을 비우고 시작하지만, KEEP_DB=1 로 올릴 때는 한 번 reset-db 가 필요하다
FROM postgres:18-alpine

# 빌드 컨텍스트가 리포지토리 루트다(compose 의 context 와 dockerfile 지정).
# 예전에는 빌드 이미지에서 인증서를 꺼냈는데, 그러면 자바 모듈을 전부 빌드해야
# 인프라도 뜰 수 있었다. 인증서는 리포지토리에 그대로 있으니 직접 복사한다.
COPY cert/xmpp.vtn.oadr.com-rsa.fingerprint /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.fingerprint
COPY docker/postgres/init-* /docker-entrypoint-initdb.d/

ENV POSTGRES_PASSWORD=supersecure

RUN vtnId=$(cat /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.fingerprint); \
	sed -i 's/%VTN_ID%/'"${vtnId}"'/g' /docker-entrypoint-initdb.d/init-openfire.sh

