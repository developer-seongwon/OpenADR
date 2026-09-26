# 앱 이미지(vtn20b, dummy-drprogram, dummy-ven20b)가 jar 를 꺼내 가는 중간 이미지(openadr_build).
# 실행하지 않고 파일만 담는다. 베이스는 앱 이미지와 같은 이미지라 따로 받을 게 없다.
#
# jar 는 run.sh(build_images)가 호스트에서 만든다(clean package -P external,frontend).
# 예전에는 maven 이미지 안에서 리포지토리 전체를 복사해 dependency:copy 를 돌리고, jar 가 없으면
# 컨테이너 안에서 다시 빌드했다. 그러면 컨테이너 안의 빈 ~/.m2 로 의존성을 전부 새로 받는다.
# 지금은 호스트 JDK(Azul Zulu 25)와 ~/.m2 캐시로 만든 결과물만 넣는다
FROM azul/zulu-openjdk:25-jre

COPY docker/build/wait-for-it.sh /opt/oadr/build/wait-for-it.sh
COPY cert /opt/oadr/cert
# VTN 의 loader.path(/opt/oadr-vtn20b/lib) 로 가는 PostgreSQL 드라이버. run.sh 가 루트 target 에 복사해 둔다
COPY target/postgresql-*.jar /opt/oadr/lib/
COPY OpenADRServerVTN20b/target/OpenADRServerVTN20b-*.jar /opt/oadr/vtn20b/
COPY DummyVEN20b/target/DummyVEN20b-*.jar /opt/oadr/dummy-ven20b/
COPY DummyDRProgram/target/DummyDRProgram-*.jar /opt/oadr/dummy-drprogram/

ENTRYPOINT ["ls", "-al"]
