# 자바 17 로 올리면서 기반 이미지도 바꾼다.
# adoptopenjdk 는 Eclipse Temurin 으로 넘어가면서 더 이상 갱신되지 않는다
FROM maven:3.9-eclipse-temurin-17

RUN mkdir /opt/oadr
RUN mkdir /opt/oadr/lib
RUN mkdir /opt/oadr/vtn20b
RUN mkdir /opt/oadr/dummy-ven20b
RUN mkdir /opt/oadr/dummy-drprogram
RUN mkdir /usr/src/openadr

COPY docker/build/wait-for-it.sh /opt/oadr/build/wait-for-it.sh
COPY cert /opt/oadr/cert
COPY . /usr/src/openadr/


RUN mvn dependency:copy@copy-external-dependency -f /usr/src/openadr/pom.xml
RUN ls -al /usr/src/openadr/target/
RUN mv /usr/src/openadr/target/* /opt/oadr/lib/

RUN if [ ! -f /usr/src/openadr/OpenADRServerVTN20b/target/OpenADRServerVTN20b-0.1.0-SNAPSHOT.jar ]; then mvn package -P external,frontend -DskipTests=true -f /usr/src/openadr/pom.xml; fi


RUN mv /usr/src/openadr/OpenADRServerVTN20b/target/*.jar /opt/oadr/vtn20b/;
RUN mv /usr/src/openadr/DummyVEN20b/target/*.jar /opt/oadr/dummy-ven20b/;
RUN mv /usr/src/openadr/DummyDRProgram/target/*.jar /opt/oadr/dummy-drprogram/;

RUN rm -rf /usr/src/openadr

ENTRYPOINT ["ls", "-al"]

