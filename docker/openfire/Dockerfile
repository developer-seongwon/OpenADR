# Openfire 4.4.4(gizmotronic/openfire, 2019 년에 멈춘 이미지) -> Openfire 5.1.2 공식 배포본으로 직접 만든다.
# Openfire 는 공식 도커 이미지를 내지 않는다. 릴리스 tar.gz 를 받아서 Azul Zulu 25 JRE 위에 푼다.
#
# 빌드 컨텍스트는 리포지토리 루트다(compose 의 context 와 dockerfile 지정).
#
# OpenADR 용 Openfire 플러그인(OpenfireOadrPlugin, 이 저장소)은 run.sh 가 호스트에서 먼저 빌드하고
# 여기서는 jar 만 넣는다. 예전에는 maven 이미지 단계에서 컨테이너 안에서 빌드했는데, 베이스 이미지가
# 바뀌면 캐시가 깨져 Openfire 의존성을 빈 저장소로 처음부터 다시 받았고, 받다가 멈춰서 20분 가까이 걸렸다.
# 호스트 빌드는 개발 PC 의 JDK(Zulu)와 ~/.m2 캐시를 그대로 쓴다.
# 그보다 전에는 github 릴리스에서 Openfire 4.4 용으로 빌드된 jar(0.0.2)를 받아 썼다.
FROM azul/zulu-openjdk:25-jre

ARG OPENFIRE_VERSION=5.1.2
ARG OPENFIRE_TARBALL=openfire_5_1_2.tar.gz

ENV OPENFIRE_HOME=/usr/share/openfire
ENV OPENFIRE_SECURITY=/usr/share/openfire/resources/security

ADD https://github.com/igniterealtime/Openfire/releases/download/v${OPENFIRE_VERSION}/${OPENFIRE_TARBALL} /tmp/openfire.tar.gz
# tar.gz 안의 openfire.sh 에는 실행 권한이 없어서 붙여 준다
RUN tar xzf /tmp/openfire.tar.gz -C /usr/share \
	&& rm /tmp/openfire.tar.gz \
	&& chmod +x "$OPENFIRE_HOME"/bin/*.sh "$OPENFIRE_HOME"/bin/openfirectl \
	&& useradd --system --home-dir "$OPENFIRE_HOME" openfire

COPY cert/oadr.com.crt /opt/oadr-vtn20b/cert/oadr.com.crt
COPY cert/xmpp.vtn.oadr.com-rsa.p12 /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.p12

# DB 접속 정보와 도메인(xmpp.vtn.oadr.com). 나머지 설정(TLS 저장소, 인증 방식, OpenADR 플러그인 설정)은
# DB 의 ofProperty 에 있다(docker/postgres/init-openfire.sh)
COPY docker/openfire/openfire.xml "$OPENFIRE_HOME/conf/openfire.xml"

COPY OpenfireOadrPlugin/target/OpenfireOadrPlugin-openfire-plugin-assembly.jar \
	"$OPENFIRE_HOME/plugins/OpenfireOadrPlugin-openfire-plugin-assembly.jar"

# 인증서 저장소. 비밀번호를 changeit 에서 changeme 로 바꾼다(init-openfire.sh 의 ofProperty 와 맞춘다).
# 저장소 형식(JKS, PKCS12)은 keytool 이 파일을 보고 알아서 맞춘다. 5.x 의 truststore 는 PKCS12 다

# 신원 저장소: 기존 비밀번호를 바꾸고 VTN 의 XMPP 인증서(xmpp.vtn.oadr.com)를 넣는다
RUN keytool -storepasswd -keystore "$OPENFIRE_SECURITY/keystore" -storepass changeit -new changeme \
	&& keytool -importkeystore -srckeystore /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.p12 -srcstoretype PKCS12 \
		-srcstorepass changeme -destkeystore "$OPENFIRE_SECURITY/keystore" -deststorepass changeme -noprompt

# 신뢰 저장소: 비밀번호를 바꾸고 OpenADR 테스트 CA 를 넣는다.
# 클라이언트 인증서 검증에도 같은 CA 를 쓰므로 client.truststore 로도 복사한다
RUN keytool -storepasswd -keystore "$OPENFIRE_SECURITY/truststore" -storepass changeit -new changeme \
	&& keytool -importcert -alias OadrCA -file /opt/oadr-vtn20b/cert/oadr.com.crt \
		-keystore "$OPENFIRE_SECURITY/truststore" -storepass changeme -noprompt \
	&& cp "$OPENFIRE_SECURITY/truststore" "$OPENFIRE_SECURITY/client.truststore"

RUN chown -R openfire:openfire "$OPENFIRE_HOME"

USER openfire
WORKDIR /usr/share/openfire

# 5222: XMPP 클라이언트, 9090: 관리 콘솔
EXPOSE 5222 9090

# openfire.sh 는 포그라운드로 자바를 띄운다. 로그는 $OPENFIRE_HOME/logs 에 남는다
CMD ["/usr/share/openfire/bin/openfire.sh"]
