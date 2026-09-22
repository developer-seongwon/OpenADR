FROM postgres:13-alpine

# 빌드 컨텍스트가 리포지토리 루트다(compose 의 context 와 dockerfile 지정).
# 예전에는 빌드 이미지에서 인증서를 꺼냈는데, 그러면 자바 모듈을 전부 빌드해야
# 인프라도 뜰 수 있었다. 인증서는 리포지토리에 그대로 있으니 직접 복사한다.
COPY cert/xmpp.vtn.oadr.com-rsa.fingerprint /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.fingerprint
COPY docker/postgres/init-* /docker-entrypoint-initdb.d/

ENV POSTGRES_PASSWORD=supersecure

RUN vtnId=$(cat /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.fingerprint); \
	sed -i 's/%VTN_ID%/'"${vtnId}"'/g' /docker-entrypoint-initdb.d/init-openfire.sh

