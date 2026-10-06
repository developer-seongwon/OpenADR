# OpenADRServerVTN20b

VTN 2.0b Spring-boot app:
- Oadr profiles 2.0a / 2.0b
- HTTP Pull and Push mode
- HTTP / XMPP transports
- HTTP Json Control API
- HTTP ReactJS frontend application
## Configuration example
```sh
oadr.server.port 9970
oadr.server.context_path /testvtn
oadr.vtnid AVOB_OPEN_ADR
oadr.supportPush true
oadr.supportUnsecuredHttpPush true
oadr.pullFrequencySeconds 30
oadr.validateOadrPayloadAgainstXsd true
oadr.report.requestOnRegister false
oadr.security.replayProtectAcceptedDelaySecond 1200
oadr.security.digest.realm oadr.avob.com


oadr.security.vtn.key /opt/oadr-vtn20b/cert/vtn.oadr.com-rsa.key
oadr.security.vtn.cert /opt/oadr-vtn20b/cert/vtn.oadr.com-rsa.crt

oadr.security.vtn.xmpp.key /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.key
oadr.security.vtn.xmpp.cert /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.crt

oadr.security.ca.key /opt/oadr-vtn20b/cert/oadr.com.key
oadr.security.ca.cert /opt/oadr-vtn20b/cert/oadr.com.crt

oadr.security.ven.trustcertificate /opt/oadr-vtn20b/cert/oadr.com.crt

oadr.broker.host rabbitmq
oadr.broker.port 5672
oadr.broker.user admin
oadr.broker.password admin

spring.datasource.url jdbc:postgresql://postgres:5432/oadr-vtn20b 
spring.datasource.username oadr-vtn20b
spring.datasource.password supersecure
spring.jpa.hibernate.ddl-auto create-drop

vtn.swagger true
vtn.cors https://vtn.oadr.com:9970,https://localhost:9970
vtn.custom-cert-folder /opt/oadr-vtn20b/cert

oadr.xmpp.host xmpp.vtn.oadr.com
oadr.xmpp.domain xmpp.vtn.oadr.com
oadr.xmpp.port 5222
```
`oadr.report.requestOnRegister` (default false): when true, the VTN answers oadrRegisterReport with one report request per registered report specifier
(granularity and reportBackDuration from the description's oadrMaxPeriod, every rID archived, reportRequestID starting with `register-`).
Use it for VENs that only take report requests from oadrRegisteredReport and never send oadrPoll.
## Build (Gradle)
The jar always contains both broker libraries (embedded ActiveMQ and RabbitMQ JMS) and the PostgreSQL driver.
The broker is chosen at runtime with the Spring profile: `standalone` (embedded ActiveMQ) or `external` (RabbitMQ).
The ReactJS frontend is built and packaged by default.
bootJar writes two identical jars to `build/libs`: `OpenADRServerVTN20b-<version>.jar` and `OpenADRServerVTN20b.jar`.
```sh
./gradlew :avob-server:OpenADRServerVTN20b:bootJar               # from the repository root
cd avob-server && ./gradlew :OpenADRServerVTN20b:bootJar         # from the avob-server directory
cd avob-server && ./gradlew :OpenADRServerVTN20b:bootJar -Pfrontend=false   # without the frontend
```
See README.kor.md / README.eng.md at the repository root for the full guide.
