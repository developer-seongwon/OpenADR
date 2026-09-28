# OpenADR

English | [한국어](README.kor.md)

Java implementation of the OpenADR protocol (https://www.openadr.org/). Spring Boot 4, Java 25.

- A standalone VTN 2.0b implementation, with a control API and web UI for demand-response programs and device management
- A VEN library to build a VEN 2.0b
- Libraries for the OpenADR model (JAXB classes generated from the XSD), security (PKI RSA/ECC, XML signature) and HTTP/XMPP clients
- A Docker demo stack that runs a VTN, VENs and a DR program together on your machine

## Contents

- [Modules](#modules)
- [Directory layout](#directory-layout)
- [Requirements](#requirements)
- [Build](#build)
- [First time: test certificates](#first-time-test-certificates)
- [First time: hosts entry](#first-time-hosts-entry)
- [Demo stack (Docker)](#demo-stack-docker)
  - [Overview](#overview)
  - [Start and endpoints](#start-and-endpoints)
  - [run.sh usage](#runsh-usage)
  - [docker directory layout](#docker-directory-layout)
  - [How it works](#how-it-works)
  - [When the database is reset](#when-the-database-is-reset)
- [Troubleshooting](#troubleshooting)
- [Links](#links)

## Modules

client/ holds the libraries. It does not depend on server.

- OpenADRSecurity: OpenADR security (PKI RSA/ECC, XML signature)
- OpenADRModel20a, OpenADRModel20b: OpenADR 2.0a and 2.0b model classes generated from the XSD
- OpenADRHTTPClient, OpenADRHTTPClient20a, OpenADRHTTPClient20b: OpenADR HTTP clients (java.net.http)
- OpenADRXMPPClient: OpenADR 2.0b XMPP client (smack)

server/ holds the servers and test applications. It takes client by coordinates (com.avob.openadr:OpenADR*).

- OpenADRServerVTNCommon: VTN common code (entities, services, control API, broker setup)
- OpenADRServerVTN20a: OpenADR 2.0a VTN
- OpenADRServerVTN20b: OpenADR 2.0b VTN, with the control API and web UI
- OpenADRServerVEN20b: OpenADR 2.0b VEN library
- OpenADRServerVTNTestSupport: shared VTN test support (PostgreSQL test container)
- DummyVEN20b: test VEN built on OpenADRServerVEN20b
- DummyDRProgram: test DR program. Manages devices and events through the VTN control API
- OpenfireOadrPlugin: Openfire plugin that authenticates XMPP VENs against the VTN (Maven)

kpx-service uses the OpenADRModel20b and OpenADRSecurity jars from client.

## Directory layout

```
settings.gradle  composite build that opens and builds client and server together. It passes no settings down
build.gradle     aggregate tasks (build, assemble, test, check, clean, publishToMavenLocal, testReport)
client/          libraries (modules above)
server/          servers and test applications (modules above), test/http/ (IntelliJ HTTP request scenarios)
docker/          local Docker stack (run.sh, compose, a Dockerfile per service). Uses the server jars and the certificates in cert
cert/            test certificates. Only generate_test_cert.sh is in git, the rest is generated (used by server tests and Docker)
```

client and server are independent Gradle builds, each with its own `settings.gradle`, `build.gradle`,
`gradle/libs.versions.toml` (version catalog) and `gradlew`. If the repository is split later, each directory becomes a repository root.
Shared settings (Java version, tests, BOM policy) are the same in both `build.gradle` files. When you change one, check the other.
Library versions live in `gradle/libs.versions.toml`. Entries without a version use the Spring Boot BOM value.

Only OpenfireOadrPlugin stays on Maven. Its parent is the Openfire plugins pom, so there is no reason to move it to Gradle.
`docker/run.sh` builds it separately.

## Requirements

Docker Desktop and Java 25. Maven is needed only to build the Openfire plugin (used by `docker/run.sh`).
The Gradle wrapper (9.7.1) downloads Gradle, and you do not need to install Node. Gradle downloads Node 24 for the frontend build.

The Java version is chosen by the Gradle toolchain (25). Gradle itself runs on any JDK 17 or later,
and compiles and tests with an installed JDK 25. If there is none, it downloads one (foojay).

Server tests start PostgreSQL with Docker and use the certificates in `cert/` at the repository root and `vtn.oadr.com` in hosts.
On a fresh machine, do the two "First time" sections below first.

## Build

Run client and server together from the repository root.

```
./gradlew build                  # build and test client and server
./gradlew assemble               # jars only, no tests (at the root, -x test cannot skip tests of the included builds)
./gradlew test                   # all tests. Prints counts per module and writes a combined report to build/reports/tests/index.html
./gradlew test --continue        # keep running the other modules' tests when one module fails
./gradlew clean
./gradlew publishToMavenLocal    # publish the client libraries to ~/.m2
./gradlew :server:OpenADRServerVTN20b:bootRun   # call a single module by path
```

You can also run each of client and server from its own directory (this is how you use it after a repository split).
Each build root has the same aggregate tasks, so they run over all modules.

```
cd client && ./gradlew build                  # build and test the libraries
cd client && ./gradlew publishToMavenLocal    # publish to ~/.m2 (to build server without client, jars for kpx-service)
cd server && ./gradlew build                  # build and test the servers (builds the sibling client from source)
cd server && ./gradlew build -x test          # jars only, no tests
cd server && ./gradlew build -Pfrontend=false # leave the React UI out of VTN20b (skips the node build, faster)
```

When a client directory sits next to server, server pulls it in with includeBuild and builds it from source,
so you do not need to install client after changing it. After a repository split, pass `-PopenadrClientDir=<client path>`,
or run `publishToMavenLocal` in client and server picks the jars from ~/.m2.

Jars go to `build/libs` of each module. VTN20a, VTN20b, DummyVEN20b and DummyDRProgram build two Spring Boot executable jars:
one with the version (`OpenADRServerVTN20b-0.1.0-SNAPSHOT.jar`) and one without (`OpenADRServerVTN20b.jar`). They are identical.
The Docker build uses the one without the version, so old jars left after a version bump do not get mixed in.
The jars for kpx-service are in `client/OpenADRModel20b/build/libs` and `client/OpenADRSecurity/build/libs`.

In IntelliJ, open the repository root. The root `settings.gradle` loads client and server together.
The aggregate tasks are under Tasks of the root (OpenADR) in the Gradle tool window, and per-module tasks are under OpenADRClient and OpenADRServer.

## First time: test certificates

In OpenADR, the VTN and VENs identify each other with certificates.
If `cert/` at the repository root has only the script, generate them once.

```
./cert/generate_test_cert.sh
```

It writes into `cert/` wherever you call it from, and stops if the certificates already exist.
It creates VTN, VEN, admin, user and app certificates under a self-signed CA.
Server tests (`../../cert/...` in `server/*/src/test/resources`) and the Docker stack use these certificates.
Without them the VTN tests fail, and `docker/run.sh` stops and asks you to generate them first.

To remove the https warning when using the VTN control API or web UI in a browser, add the CA certificate `cert/oadr.com.crt`
as a trusted certificate in the browser (or OS). Without it you only get a warning and can proceed.
With the admin client certificate `cert/admin.oadr.com.p12` (password changeme) installed, you can also log in with x509.
Otherwise log in with admin / admin.

## First time: hosts entry

The VTN certificate is issued for `vtn.oadr.com`, and the VEN20b XMPP tests connect with that name too.
Without a hosts entry the name goes to public DNS, times out, and the tests fail.

```
echo "127.0.0.1 vtn.oadr.com" | sudo tee -a /etc/hosts
```

## Demo stack (Docker)

### Overview

Runs a full setup on your machine with the VTN 2.0b in the middle, a test VEN (dummy-ven20b) and a DR program (dummy-drprogram).

- dummy-ven20b: VEN simulator. Connects to the VTN over HTTP (simpleHttp) and XMPP (through Openfire). Only the VENs enabled in
  `docker/service/client/dummy-ven20b/application.properties` connect (currently ven2 over xmpp and ven3 over simpleHttp).
  VENs authenticate with x509 client certificates. They simulate readings from the DR events they receive and keep sending reports.
- vtn20b: the VTN. Data is stored in PostgreSQL, and XMPP goes through Openfire.
- dummy-drprogram: stands where the operating system behind the VTN would be. Through the VTN control API it creates market contexts
  and VENs, subscribes to reports and creates DR events (device management and DR program management).
  The VTN notifies it through RabbitMQ of what it receives from VENs (registration, register report, update report).

<details>
	<summary>PlantUML components diagram</summary>
	```
	@startuml demo_component_diagram

	package "Demand / Production" {
	    rectangle "dummy-ven20b" as dummyVen #FFF
	}

	package "OADR Provider" {
	    rectangle "vtn20b" as vtn #FFF
	    database postgres
	    node rabbitmq
	    node openfire
	}

	package "DemandResponseProgram" {
	    rectangle "dummy-drprogram" as dummyDRProgram #FFF
	}


	vtn <-up-> openfire #line:red;line.bold;text:red  : OADR(XMPP)
	openfire -> vtn #green;line.bold;text:green : AUTH(HTTP)
	vtn -down-> rabbitmq #blue;line.bold;text:blue   : DATA(AMQP)
	dummyVen <--> vtn #green;line.bold;text:green : OADR(HTTP)
	dummyVen <-> openfire #line:red;line.bold;text:red  : OADR(XMPP)
	openfire -> postgres #black;line.dotted;text:black
	vtn -> postgres #black;line.dotted;text:black
	rabbitmq -down-> vtn #green;line.bold;text:green : AUTH(HTTP)
	dummyDRProgram -up-> vtn #green;line.bold;text:green : DATA(HTTP)
	dummyDRProgram <-- rabbitmq #blue;line.bold;text:blue   : DATA(AMQP)

	@enduml
	```

</details>

![](demo_component_diagram.png)

The AUTH(HTTP) arrow from rabbitmq to vtn in the diagram is from the old setup. RabbitMQ now uses only its internal accounts and does not ask the VTN.

<details>
	<summary>PlantUML sequence diagram</summary>
	```
	@startuml demo_sequence_diagram

	participant "dummy-ven20b" as dummyVen #FFF
	participant "vtn20b" as vtn #FFF
	participant "dummy-drprogram" as dummyDRProgram #FFF

	group Device provisionning
	dummyDRProgram -[#green]> vtn: Creates MarketContext / VEN
	dummyDRProgram -[#green]> vtn: Enrolls VEN to MarketContext
	end 

	group Device registration
	dummyVen -[#red]> vtn: Creates registration party
	vtn -[#blue]> dummyDRProgram: Notify registration



	dummyVen -[#red]> vtn: Registers reports
	vtn -[#blue]> dummyDRProgram: Notify register reports
	dummyDRProgram-[#green]> vtn: Subscribes reports
	vtn -[#red]> dummyVen: Creates reports subscription
	end

	group Normal workflow
	group DRProgram
	dummyDRProgram -[#green]> vtn: Creates DREvents in MarketContext
	dummyVen <[#red]- vtn: Send DREvents
	end
	group Data reading
	dummyVen -[#black]-> dummyVen: Simulate data readings\n based on received DREvents
	dummyVen -[#red]> vtn: Updates reports
	vtn -[#blue]> dummyDRProgram: Notify data update
	end

	end

	@enduml
	```
</details>

![](demo_sequence_diagram.png)

### Start and endpoints

```
./docker/run.sh start all
```

All `./docker/run.sh` commands in this document are run from the repository root. The script moves to the repository root wherever you call it from.
It builds the server with `server/gradlew` and uses the certificates in `cert` (`SERVER_DIR` changes the server location).

It builds the jars locally first (`./gradlew assemble`), then builds the images and starts the containers.
A client directory next to server is built together. After a repository split, pass the client path with `CLIENT_DIR`,
or run `publishToMavenLocal` in client beforehand.
`docker/run.sh` finds `mvn` for the Openfire plugin by itself. If it cannot, it stops with an error.
Pass the `MVN` environment variable to set it yourself.
The first run takes a few minutes including the image builds.

Once it is up:

- VTN web UI: https://localhost:8181/testvtn/ (admin / admin). The browser warns about the self-signed certificate. Just proceed
- VTN control API docs (Swagger UI): https://localhost:8181/testvtn/swagger-ui/index.html
- API schema: https://localhost:8181/testvtn/v3/api-docs (the API docs and schema open without login)
- RabbitMQ management: http://localhost:15672 (admin / admin)
- Openfire admin: http://localhost:9090
- Dummy VEN: https://localhost:8083. It is https and requires a client certificate, so there is little to see in a browser

### run.sh usage

```
./docker/run.sh <command> [target]
```

command is one of `start`, `stop`, `restart`, `logs`, `status`, `build`, `reset-db`.
`build` only builds the jars (`./gradlew assemble`).

target is given in three ways.
`infra` starts only postgres, rabbitmq and openfire. Use it when you run the applications in IntelliJ.
`all` starts the infrastructure and all applications. It is the default when omitted.
You can also name services: `vtn20b`, `dummy-drprogram`, `dummy-ven20b`.
List several with commas or spaces and they start in that order.

```
./docker/run.sh start infra
./docker/run.sh restart vtn20b
./docker/run.sh logs vtn20b,dummy-ven20b
./docker/run.sh status
./docker/run.sh stop all
```

The infrastructure and the applications are one stack (compose project oadr). You can start the infrastructure with `start infra`
and add applications later, for example `start vtn20b`.
Openfire reaches the VTN even when only the infrastructure runs in Docker and the VTN runs in IntelliJ. Openfire sends vtn.oadr.com
to the host (host-gateway), so it reaches the host port mapping 8181 when the VTN is a container, or the VTN on the host when it runs in IntelliJ.

### docker directory layout

```
docker/
  run.sh                       start script. This is the only entry point you need
  postgres/                    database. The init scripts create the oadr-vtn20b and openfire schemas
  rabbitmq/                    message broker
  openfire/                    XMPP server. Used when a VEN connects over xmpp
  service/                     applications built from this repository
    build/                     intermediate image holding the jars (openadr_build). The application images copy their jars from it
    server/                    VTN side of OpenADR
      vtn20b/                  VTN 2.0b server (with web UI)
      dummy-drprogram/         DR program simulator (the operating system behind the VTN). Creates events and receives reports
    client/                    VEN side of OpenADR
      dummy-ven20b/            VEN simulator. Connects over http, simpleHttp and xmpp
```

Each directory holds the service's `Dockerfile`, `docker-compose.yml` and configuration files.
`run.sh` passes all these compose files with `-f` as one project (oadr), and the command only chooses which services to start.
To add a service, create its directory and add one line to `COMPOSE_FILES` in `run.sh`.

Paths inside the compose files are relative to the repository root. With several `-f` files, compose resolves relative paths
against one base directory, so `run.sh` passes the repository root with `--project-directory`.
`service/build`, `postgres`, `rabbitmq` and `openfire` use the repository root as build context,
because they need the built jars (`server/*/build/libs`) and `cert/`. The root `.dockerignore` (an allow list of what the images COPY) applies.
The other application images use their own directory as context.

### How it works

All three application images copy their jars from the `openadr_build` image,
so that image has to be built before the applications start, and `run.sh` keeps that order.

The jars are built locally with Gradle, not inside a container.
The VTN20b jar always contains both broker libraries (embedded ActiveMQ broker, RabbitMQ JMS) and the PostgreSQL driver.
The broker is chosen only by the Spring profile: `standalone` for embedded ActiveMQ, `external` for RabbitMQ.
With Maven, a Maven profile put in only one of them, and a Docker build without `-P external` made the VTN die
because it could not find `RMQConnectionFactory`. That trap is gone.
DummyDRProgram also contains both the ActiveMQ and RabbitMQ clients and chooses by Spring profile.

The frontend is in `server/OpenADRServerVTN20b/frontend` and is built with Vite (react-scripts before).
`npm run build` writes to `frontend/build`, and Gradle puts it directly into `public/` of the jar
(`frontendBuild` task, which does not run again unless its inputs change).
`src/main/resources/public`, where Maven used to copy it, is no longer used, and anything left there is kept out of the jar.
When you run the VTN from IntelliJ through Gradle (the default), the UI comes up too.
The bundles live under `static/`, because `HttpSecurityConfig` opens `/static/**` without authentication.
Files containing JSX must have the `.jsx` extension. Vite does not read JSX in `.js` files.

The UI uses React 19, MUI 9 and react-router 8. The screens were originally drawn with MUI 3,
so `src/theme.js` restores the MUI 3 defaults (colors, input style, Grid width, tab width, table font).
If a screen looks odd, start there. Component styles are applied with `withStyles` from `tss-react`.

The VTN runs with the `fake-data,rabbitmq-broker,external` profiles.
`fake-data` seeds the market contexts and the initial accounts.

### When the database is reset

A start that includes the infrastructure resets the database, so every run starts from the same state.

```
./docker/run.sh start all        # reset, then start
./docker/run.sh restart all      # reset, then start
./docker/run.sh start infra      # reset, then start
```

Restarting a single service does not reset it, because losing data when you only change a setting
and restart vtn20b would get in the way.

```
./docker/run.sh restart vtn20b   # database kept
```

To start everything and keep the database, pass `KEEP_DB=1`.

```
KEEP_DB=1 ./docker/run.sh restart all
```

To reset only the database:

```
./docker/run.sh reset-db
```

Resetting deletes the postgres data volume (`oadr_pgdata`).
The data lives in the volume, so it survives recreating the container and goes away only when you reset it.
`ddl-auto` is `update`, which keeps the schema, and resetting the volume recreates the schema as well.
Changes such as narrowing a column type or dropping a column in an entity are applied at that point.
`docker/postgres/init-*.sh` runs only on an empty volume, so it runs again too.

## Troubleshooting

If the VEN list is empty, the dummies have not registered yet.
After a restart they register again within one to two minutes.

```
./docker/run.sh restart dummy-drprogram,dummy-ven20b
```

If a port is reported as already in use, an old stack is still running.

```
./docker/run.sh stop all
```

`ERR_CONNECTION_RESET` on 8083 is expected. It is https, not http,
and requires a client certificate.

If Gradle cannot find a Java 25 toolchain, install JDK 25 or check the network (it downloads through foojay).

The VEN20b tests open real server sockets on ports 18081 and 18082.
They are set in `server/OpenADRServerVEN20b/src/test/resources/application.properties`,
and `OadrMockMvc` puts the port in the request URL, so change both together.

Tests start PostgreSQL with Docker. If Docker is not running, the context cannot start.
One container is reused per JVM, and its holder is in the `OpenADRServerVTNTestSupport`
module. It is a test-only module and is not part of any deliverable.

The VTNCommon, VTN20a and VTN20b tests all start a VTN on port 8182, so Gradle runs the tests of these modules
one at a time (serialTests in server/build.gradle). Compilation stays parallel.

Opening an SPA route directly or refreshing it must not return 404.
Paths such as `/ven` and `/event/detail/...` are served index.html by `SpaIndexController`.
When you add a frontend route, add the path to both that controller and `HttpSecurityConfig`.

## Links

- [OpenADR 2.0b Spec](https://cimug.ucaiug.org/Projects/CIM-OpenADR/Shared%20Documents/Source%20Documents/OpenADR%20Alliance/OpenADR_2_0b_Profile_Specification_v1.0.pdf)
- [DRProgram Guide v1.0](https://www.openadr.org/assets/openadr_drprogramguide_v1.0.pdf)
