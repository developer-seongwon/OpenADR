# 자바 17 런타임. openjdk 공식 이미지는 폐기됐고 Temurin 이 후속이다
FROM eclipse-temurin:17-jre

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

# VTN 은 8181 로 뜬다. 예전 설정에 8081 이 남아 있어서 여기서 150초를 그냥 기다리다
# VTN 이 준비된 줄 모르고 뒤늦게 기동했다
CMD ["/opt/oadr-ven20b/wait-for-it.sh", "vtn.oadr.com:8181", "-t", "150", "--" \
	, "java", "-jar", "/opt/oadr-ven20b/oadr-ven20b.jar", "--spring.config.location=/opt/oadr-ven20b/application.properties"]