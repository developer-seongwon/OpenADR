# OpenADR

OpenADR 2.0a/2.0b Java 구현(VTN, VEN, 클라이언트 라이브러리). Spring Boot 4 / Java 25.
OpenADR 2.0a/2.0b Java implementation (VTN, VEN, client libraries). Spring Boot 4 / Java 25.

- [한국어](README.kor.md)
- [English](README.eng.md)

두 문서는 같은 내용이다(모듈, 빌드, 인증서, 도커 데모 스택, 문제 해결).
Both documents have the same content (modules, build, certificates, Docker demo stack, troubleshooting).

```
./cert/generate_test_cert.sh    # 최초 1회 테스트 인증서 / test certificates (first time only)
./gradlew build                 # client, server 빌드와 테스트 / build and test
./docker/run.sh start all       # 로컬 도커 스택 / local docker stack
```
