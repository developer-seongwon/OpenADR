# 자바 17 런타임. openjdk 공식 이미지는 폐기됐고 Temurin 이 후속이다
FROM eclipse-temurin:17-jre

RUN mkdir /opt/oadr-vtn20b/
RUN mkdir /opt/oadr-vtn20b/lib
RUN mkdir /var/log/oadr-vtn20b
RUN mkdir /var/run/oadr-vtn20b

COPY --from=openadr_build:latest /opt/oadr/build/wait-for-it.sh /opt/oadr-vtn20b/wait-for-it.sh
COPY --from=openadr_build:latest /opt/oadr/cert /opt/oadr-vtn20b/cert
COPY --from=openadr_build:latest /opt/oadr/vtn20b/*.jar /opt/oadr-vtn20b/
COPY --from=openadr_build:latest /opt/oadr/lib/* /opt/oadr-vtn20b/lib/

COPY application.properties /opt/oadr-vtn20b/application.properties
COPY loader.properties /opt/oadr-vtn20b/loader.properties
COPY log4j2.xml /opt/oadr-vtn20b/log4j2.xml
COPY oadr-vtn20b.conf /opt/oadr-vtn20b/oadr-vtn20b.conf
RUN mv /opt/oadr-vtn20b/*.jar /opt/oadr-vtn20b/oadr-vtn20b.jar

RUN chmod +x /opt/oadr-vtn20b/wait-for-it.sh

# CMD 는 마지막 것만 유효하다. 예전에는 위에 wait-for-it 을 쓰는 CMD 가 하나 더 있었는데
# 경로가 ven20b 로 잘못 적혀 있었고 어차피 아래 것에 덮여서 한 번도 실행된 적이 없다.
# 부트 실행 가능 jar 라 jar 를 그대로 CMD 로 둔다. 설정은 oadr-vtn20b.conf 가 넘긴다.
CMD ["/opt/oadr-vtn20b/oadr-vtn20b.jar"]