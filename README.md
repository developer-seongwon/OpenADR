# OpenADR

OpenADR 2.0a/2.0b Java 구현(VTN, VEN, 클라이언트 라이브러리). Spring Boot 4 / Java 25.
OpenADR 2.0a/2.0b Java implementation (VTN, VEN, client libraries). Spring Boot 4 / Java 25.

[avob/OpenADR](https://github.com/avob/OpenADR) 를 포크해서 Spring Boot 4, Java 25 로 옮기고 고친 것이다. 라이선스는 원본과 같은 Apache 2.0([LICENSE](LICENSE)).
Forked from [avob/OpenADR](https://github.com/avob/OpenADR), moved to Spring Boot 4 / Java 25 with fixes. Same Apache 2.0 license as the original ([LICENSE](LICENSE)).

- [한국어](README.kor.md)
- [English](README.eng.md)

두 문서는 같은 내용이다(모듈, 빌드, 인증서, 도커 데모 스택, 문제 해결).
Both documents have the same content (modules, build, certificates, Docker demo stack, troubleshooting).

```
./cert/generate_test_cert.sh    # 최초 1회 테스트 인증서 / test certificates (first time only)
./gradlew build                 # avob-client, avob-server 빌드와 테스트 / build and test
./docker/run.sh start all       # 로컬 도커 스택 / local docker stack
```
