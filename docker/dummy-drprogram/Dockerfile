# 자바 25 런타임. 빌드 이미지(docker/build)의 JDK 와 같은 메이저라야 한다.
# openjdk 공식 이미지는 폐기됐고 Temurin 이 후속이다
FROM eclipse-temurin:25-jre

RUN mkdir /opt/dummy-drprogram
RUN mkdir /var/log/dummy-drprogram
RUN mkdir /var/run/dummy-drprogram

COPY --from=openadr_build:latest /opt/oadr/build/wait-for-it.sh /opt/dummy-drprogram/wait-for-it.sh
COPY --from=openadr_build:latest /opt/oadr/cert /opt/dummy-drprogram/cert
COPY --from=openadr_build:latest /opt/oadr/dummy-drprogram/*.jar /opt/dummy-drprogram/

COPY simple.DemandResponseEventCreateDto.json /opt/dummy-drprogram/simple.DemandResponseEventCreateDto.json
COPY fastDR.DemandResponseEventCreateDto.json /opt/dummy-drprogram/fastDR.DemandResponseEventCreateDto.json
COPY thermostat.DemandResponseEventCreateDto.json /opt/dummy-drprogram/thermostat.DemandResponseEventCreateDto.json

COPY application.properties /opt/dummy-drprogram/application.properties

RUN mv /opt/dummy-drprogram/*.jar /opt/dummy-drprogram/dummy-drprogram.jar

RUN chmod +x /opt/dummy-drprogram/wait-for-it.sh

# VTN 은 8181 로 뜬다. 예전 설정에 8081 이 남아 있어서 여기서 150초를 그냥 기다리다
# VTN 이 준비된 줄 모르고 뒤늦게 기동했다
CMD ["/opt/dummy-drprogram/wait-for-it.sh", "vtn.oadr.com:8181", "-t", "120", "--" \
	, "java", "-jar", "/opt/dummy-drprogram/dummy-drprogram.jar", "--spring.config.location=/opt/dummy-drprogram/application.properties"]