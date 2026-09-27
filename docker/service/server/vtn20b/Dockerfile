# 자바 25 런타임. 개발 PC 의 JDK(Azul Zulu 25)와 같은 배포판인 Azul 공식 이미지를 쓴다.
# 예전에는 eclipse-temurin:25-jre 였다
FROM azul/zulu-openjdk:25-jre

RUN mkdir /opt/oadr-vtn20b/
RUN mkdir /opt/oadr-vtn20b/lib
RUN mkdir /var/log/oadr-vtn20b
RUN mkdir /var/run/oadr-vtn20b

COPY --from=openadr_build:latest /opt/oadr/build/wait-for-it.sh /opt/oadr-vtn20b/wait-for-it.sh
COPY --from=openadr_build:latest /opt/oadr/cert /opt/oadr-vtn20b/cert
COPY --from=openadr_build:latest /opt/oadr/vtn20b/*.jar /opt/oadr-vtn20b/

COPY application.properties /opt/oadr-vtn20b/application.properties
COPY loader.properties /opt/oadr-vtn20b/loader.properties
COPY log4j2.xml /opt/oadr-vtn20b/log4j2.xml
RUN mv /opt/oadr-vtn20b/*.jar /opt/oadr-vtn20b/oadr-vtn20b.jar

RUN chmod +x /opt/oadr-vtn20b/wait-for-it.sh

# PropertiesLauncher 가 loader.properties 를 현재 디렉토리에서 찾는다.
# 거기에 loader.path 가 들어 있어서 lib 의 외부 jar 들이 얹힌다.
# 지금은 필요한 jar(PostgreSQL 드라이버, 두 브로커 라이브러리)가 전부 실행 jar 안에 있어서 lib 는 비어 있다.
# 운영에서 jar 를 다시 만들지 않고 라이브러리를 얹거나 바꿔 끼울 때 쓰는 자리로 남겨 둔다
WORKDIR /opt/oadr-vtn20b

# CMD 는 마지막 것만 유효하다. 예전에는 위에 wait-for-it 을 쓰는 CMD 가 하나 더 있었는데
# 경로가 ven20b 로 잘못 적혀 있었고 어차피 아래 것에 덮여서 한 번도 실행된 적이 없다.
#
# 예전에는 부트의 실행 가능 jar 기능(<executable>true</executable>)을 써서 jar 를
# 그대로 CMD 에 뒀고, 그 jar 앞에 붙은 기동 스크립트가 oadr-vtn20b.conf 의
# JAVA_OPTS 와 RUN_ARGS 를 읽어 넘겨줬다. 부트 4 가 그 기능을 없애면서 jar 가
# 평범한 zip 이 되어 "Permission denied" 로 죽었다.
# 이제는 다른 서비스들처럼 java -jar 로 띄우고 인자를 직접 적는다.
CMD ["java", "-Xmx256M", "-jar", "/opt/oadr-vtn20b/oadr-vtn20b.jar" \
	, "--spring.config.location=/opt/oadr-vtn20b/application.properties" \
	, "--logging.config=/opt/oadr-vtn20b/log4j2.xml" \
	, "--spring.profiles.active=fake-data,rabbitmq-broker,external"]