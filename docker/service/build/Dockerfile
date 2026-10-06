# 앱 이미지(vtn20b, dummy-drprogram, dummy-ven20b)가 jar 를 꺼내 가는 중간 이미지(openadr_build).
# 실행하지 않고 파일만 담는다. 베이스는 앱 이미지와 같은 이미지라 따로 받을 게 없다.
#
# jar 는 run.sh(build_images)가 호스트에서 그래들로 만든다(./gradlew assemble, 각 모듈 build/libs).
# 예전에는 maven 이미지 안에서 리포지토리 전체를 복사해 dependency:copy 를 돌리고, jar 가 없으면
# 컨테이너 안에서 다시 빌드했다. 그러면 컨테이너 안의 빈 저장소로 의존성을 전부 새로 받는다.
# 지금은 호스트 JDK(Azul Zulu 25)와 그래들 캐시로 만든 결과물만 넣는다.
#
# 빌드 컨텍스트는 저장소 루트다(compose 의 context 가 ..). 서버 jar 는 avob-server/ 아래, 인증서는 cert/ 에 있다
FROM azul/zulu-openjdk:25-jre

COPY docker/service/build/wait-for-it.sh /opt/oadr/build/wait-for-it.sh
COPY cert /opt/oadr/cert
# PostgreSQL 드라이버는 이제 VTN 실행 jar 안에 들어 있다. 예전에는 메이븐 external 프로파일 jar 에 없어서
# server/target 에 따로 복사해 loader.path(/opt/oadr-vtn20b/lib)로 얹었다
# 버전 없는 이름의 실행 jar 를 가져간다(avob-server/build.gradle 의 bootJar 주석 참고)
COPY avob-server/OpenADRServerVTN20b/build/libs/OpenADRServerVTN20b.jar /opt/oadr/vtn20b/
COPY avob-server/DummyVEN20b/build/libs/DummyVEN20b.jar /opt/oadr/dummy-ven20b/
COPY avob-server/DummyDRProgram/build/libs/DummyDRProgram.jar /opt/oadr/dummy-drprogram/

ENTRYPOINT ["ls", "-al"]
