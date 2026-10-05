# 자바 25 런타임. 개발 PC 의 JDK(Azul Zulu 25)와 같은 배포판인 Azul 공식 이미지를 쓴다.
# 예전에는 eclipse-temurin:25-jre 였다
FROM azul/zulu-openjdk:25-jre

RUN mkdir /opt/oadr-ven20b
RUN mkdir /var/log/oadr-ven20b
RUN mkdir /var/run/oadr-ven20b

COPY --from=openadr_build:latest /opt/oadr/build/wait-for-it.sh /opt/oadr-ven20b/wait-for-it.sh
COPY --from=openadr_build:latest /opt/oadr/cert /opt/oadr-ven20b/cert
COPY --from=openadr_build:latest /opt/oadr/dummy-ven20b/*.jar /opt/oadr-ven20b/

COPY realEnergy.registerReport.xml /opt/oadr-ven20b/realEnergy.registerReport.xml
COPY smartEnergyModule.registerReport.xml /opt/oadr-ven20b/smartEnergyModule.registerReport.xml
COPY thermostat.registerReport.xml /opt/oadr-ven20b/thermostat.registerReport.xml
COPY thermostat.full.registerReport.xml /opt/oadr-ven20b/thermostat.full.registerReport.xml


COPY application.properties /opt/oadr-ven20b/application.properties
RUN mv /opt/oadr-ven20b/*.jar /opt/oadr-ven20b/oadr-ven20b.jar

RUN chmod +x /opt/oadr-ven20b/wait-for-it.sh

# VTN 은 9970 으로 뜬다(application.properties 의 oadr.server.port).
# 포트를 9970 대로 옮길 때 여기만 8181 로 남아서, 열리지 않는 포트를 150초 꽉 채워 기다린 뒤에야 떴다.
# 기동 순서는 compose 의 depends_on(vtn20b healthy)이 맞추고 이건 compose 밖에서 띄울 때를 위한 안전장치다
CMD ["/opt/oadr-ven20b/wait-for-it.sh", "vtn.oadr.com:9970", "-t", "150", "--" \
	, "java", "-jar", "/opt/oadr-ven20b/oadr-ven20b.jar", "--spring.config.location=/opt/oadr-ven20b/application.properties"]