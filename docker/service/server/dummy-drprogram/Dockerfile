# 자바 25 런타임. 개발 PC 의 JDK(Azul Zulu 25)와 같은 배포판인 Azul 공식 이미지를 쓴다.
# 예전에는 eclipse-temurin:25-jre 였다
FROM azul/zulu-openjdk:25-jre

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

# VTN 은 9970 으로 뜬다(application.properties 의 oadr.server.port).
# 포트를 9970 대로 옮길 때 여기만 8181 로 남아서, 열리지 않는 포트를 120초 꽉 채워 기다린 뒤에야 떴다
# (그래서 DummyMarketContext 가 2분쯤 늦게 생겼다). 기동 순서는 compose 의 depends_on(vtn20b healthy)이 맞추고
# 이건 compose 밖에서 띄울 때를 위한 안전장치다
CMD ["/opt/dummy-drprogram/wait-for-it.sh", "vtn.oadr.com:9970", "-t", "120", "--" \
	, "java", "-jar", "/opt/dummy-drprogram/dummy-drprogram.jar", "--spring.config.location=/opt/dummy-drprogram/application.properties"]