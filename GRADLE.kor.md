# 그래들 빌드 스크립트 정리

이 저장소의 그래들 스크립트(Groovy DSL, 그래들 9.7.1)를 읽고 고칠 때 필요한 문법과 이 저장소의 작성 규칙이다.
예시는 전부 이 저장소의 실제 파일에서 가져왔다. 빌드 명령과 도커는 README.kor.md 를 본다.

## 목차

- [파일 구성](#파일-구성)
- [작성 규칙](#작성-규칙)
- [settings.gradle](#settingsgradle)
- [버전 카탈로그(libs.versions.toml)](#버전-카탈로그libsversionstoml)
- [플러그인](#플러그인)
- [의존성과 configuration](#의존성과-configuration)
- [BOM(버전 맞추기)](#bom버전-맞추기)
- [태스크](#태스크)
- [지연 값(Provider)과 경로](#지연-값provider과-경로)
- [공통 설정 나누기](#공통-설정-나누기)
- [프로퍼티(-P)](#프로퍼티-p)
- [자주 쓰는 명령](#자주-쓰는-명령)
- [메이븐과 다른 점, 걸렸던 것](#메이븐과-다른-점-걸렸던-것)

## 파일 구성

```
settings.gradle          빌드에 들어갈 프로젝트(모듈)를 정한다. 빌드마다 하나
build.gradle             프로젝트 설정. 빌드 루트와 모듈마다 하나
gradle.properties        그래들 옵션(병렬, 캐시, JVM 메모리)과 프로젝트 값(group, version)
gradle/libs.versions.toml 버전 카탈로그(라이브러리, 플러그인 버전 목록)
gradlew, gradle/wrapper/ wrapper. 정해진 그래들 버전(9.7.1)을 받아서 돌린다. 그래들을 따로 깔 필요 없다
```

빌드는 세 개다. oadr-client 와 oadr-server 가 각자 독립된 빌드이고, 저장소 루트는 둘을 묶는 composite 빌드다.

```
OpenADR(루트)       settings.gradle(includeBuild oadr-client, oadr-server), build.gradle(묶음 태스크)
  oadr-client       settings.gradle, build.gradle(공통 설정), gradle/libs.versions.toml, 모듈 7개
  oadr-server       settings.gradle, build.gradle(공통 설정), gradle/libs.versions.toml, 모듈 7개
```

## 작성 규칙

이 저장소 스크립트는 아래 규칙으로 맞춰 두었다. 고칠 때도 맞춘다.

- 플러그인은 `plugins { }` 블록으로 붙인다. `subprojects { }` 안에서는 plugins 블록을 못 써서 거기만 `apply plugin:` 을 쓴다.
- 모듈 파일 순서: `import`, `plugins`, `description`, configuration 선언, `dependencies`, 플러그인 설정 블록(`springBoot`, `node`, `openApiGenerate`), 태스크.
  생성 태스크(xjc, openApiGenerate)의 결과를 sourceSets 에 잇는 줄은 그 태스크 바로 뒤에 둔다.
- `dependencies` 블록은 파일에 하나만 둔다.
- 문자열은 작은따옴표. `${}` 를 넣을 때만 큰따옴표를 쓴다. 큰따옴표 문자열(GString)을 문자열 자리에 넘길 때는 `.toString()` 을 붙인다.
- 인자 하나짜리 DSL 호출은 괄호 없이 쓴다(`include 'X'`, `api libs.x`, `includeBuild clientDir`). 뒤에 클로저가 붙거나 호출을 이어 붙일 때만 괄호를 쓴다.
- 클래스는 긴 이름 대신 파일 맨 위에서 `import` 한다(`NpmTask`, `Files`, `XmlSlurper`).
- 프로퍼티는 `=` 로 넣는다(`mainClass = '...'`). 예전 문법(`mainClass '...'`, `.set(...)`)은 쓰지 않는다.
- 태스크는 `tasks.register`(새로 만들기)와 `tasks.named`(있는 것 고치기)만 쓴다. `task x { }`, `tasks.getByName` 은 쓰지 않는다.
- 버전은 스크립트에 적지 않고 `libs.versions.toml` 에 적는다.
- 주석은 한글. 왜 그렇게 했는지를 적는다.

## settings.gradle

빌드에 어떤 프로젝트가 들어가는지 정한다. `build.gradle` 보다 먼저 읽힌다.

```groovy
// oadr-client/settings.gradle
plugins {
    // 자바 25 가 없는 PC 에서도 툴체인을 받아 오게 한다
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}

rootProject.name = 'OpenADRClient'

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

include 'OpenADRSecurity'
include 'OpenADRModel20a'
```

- `include 'X'` 는 디렉토리 X 를 모듈(:X)로 넣는다. X 안의 build.gradle 이 그 모듈 설정이다.
- `dependencyResolutionManagement.repositories` 는 모든 모듈이 같이 쓰는 저장소다.
  `FAIL_ON_PROJECT_REPOS` 라서 모듈 build.gradle 에 `repositories { }` 를 쓰면 에러가 난다. 저장소는 여기에만 둔다.
- 저장소마다 받을 것을 좁힐 수 있다. oadr-server 는 mavenLocal 에서 우리 그룹만 받는다.

```groovy
// oadr-server/settings.gradle
mavenLocal {
    content {
        includeGroup 'com.avob.openadr'
    }
}
```

- `includeBuild 경로` 는 다른 그래들 빌드를 통째로 물고 들어온다(composite build).
  물린 빌드가 만드는 좌표(group:name)를 쓰는 의존성은 저장소에서 받지 않고 그 빌드의 소스로 바로 만든다.
  oadr-server 는 옆에 oadr-client 가 있으면 이렇게 해서 `com.avob.openadr:OpenADRModel20b` 를 oadr-client 소스로 받는다.

```groovy
// oadr-server/settings.gradle
def clientDir = file(providers.gradleProperty('openadrClientDir').getOrElse('../oadr-client'))
if (new File(clientDir, 'settings.gradle').isFile()) {
    includeBuild clientDir
}
```

## 버전 카탈로그(libs.versions.toml)

버전과 좌표를 한 곳에 모은다. 네 구역이 있다.

```toml
[versions]
smack = "4.5.0"

[libraries]
smack-tcp = { module = "org.igniterealtime.smack:smack-tcp", version.ref = "smack" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter" }          # 버전 없음: BOM 값을 쓴다

[bundles]
smack = ["smack-tcp", "smack-im", "smack-extensions", "smack-resolver-dnsjava", "smack-java11"]

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
```

스크립트에서는 `libs` 로 꺼낸다. 이름의 `-` 는 `.` 이 된다.

```groovy
api libs.smack.tcp                          // [libraries] smack-tcp
api libs.bundles.smack                      // [bundles] smack(여러 개 한 번에)
version = libs.versions.nodejs.get()        // [versions] nodejs. 값은 .get() 으로 꺼낸다
alias(libs.plugins.spring.boot) apply false // [plugins] spring-boot
```

- 이름이 겹치면 안 된다. `node` 버전과 `node-gradle` 플러그인 버전을 같이 두었더니 `libs.versions.node` 가
  `node.gradle` 의 부모가 돼서 `.get()` 을 못 불렀다. 그래서 `nodejs` 로 바꿨다.
- `subprojects { }` 안에서 `libs` 를 바로 쓰면 하위 모듈에서 찾다가 루트로 올라가는 암묵 조회가 돼서 그래들 10 에서 막힌다.
  루트에서 `def catalog = libs` 로 잡아 두고 `catalog.x` 로 쓴다. 모듈 build.gradle 에서는 `libs` 를 그대로 쓴다.

## 플러그인

```groovy
// oadr-server/build.gradle(빌드 루트): 버전만 정하고 붙이지 않는다
plugins {
    id 'base'
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.openapi.generator) apply false
    alias(libs.plugins.node.gradle) apply false
}

// oadr-server/OpenADRServerVTN20b/build.gradle(모듈): 버전 없이 붙인다
plugins {
    id 'org.springframework.boot'
    id 'com.github.node-gradle.node'
}
```

- `apply false` 는 플러그인을 받아만 두고 이 프로젝트에는 붙이지 않는다. 모듈은 버전 없이 id 로 붙인다.
  모듈에서 버전까지 다시 적으면 "이미 클래스패스에 있다" 에러가 난다.
- 그래들 기본 플러그인(`java-library`, `jacoco`, `maven-publish`, `base`)은 버전이 없다.
- 플러그인을 붙이면 그 플러그인의 설정 블록(extension)과 태스크가 생긴다.

```groovy
java {                    // java 플러그인
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
springBoot {              // 스프링 부트 플러그인
    mainClass = 'com.avob.openadr.server.oadr20b.vtn.VTN20bApplication'
}
jacoco {                  // jacoco 플러그인
    toolVersion = catalog.versions.jacoco.get()
}
```

## 의존성과 configuration

`dependencies { }` 안의 `api`, `implementation` 같은 이름이 configuration 이다. 어느 클래스패스에 들어가는지를 정한다.

- `api`: 컴파일, 실행 둘 다. 이 모듈을 쓰는 쪽의 컴파일에도 넘어간다(`java-library` 플러그인). 라이브러리가 공개 API 로 노출하는 의존성.
- `implementation`: 컴파일, 실행 둘 다. 쓰는 쪽 컴파일에는 안 넘어간다.
- `compileOnly`: 컴파일에만. 실행 jar 에는 안 들어간다. 메이븐 provided 와 비슷하지만 테스트 클래스패스에도 안 들어간다.
- `runtimeOnly`: 실행에만(PostgreSQL 드라이버).
- `testImplementation`, `testRuntimeOnly`: 테스트용.

```groovy
dependencies {
    api project(':OpenADRSecurity')               // 같은 빌드의 다른 모듈
    api libs.openadr.httpclient                   // 좌표(includeBuild 로 물린 oadr-client 에서 온다)
    compileOnly libs.bundles.vtn.brokers
    runtimeOnly libs.postgresql
    testImplementation libs.spring.boot.starter.test

    // 끌려오는 의존성 빼기. 뒤에 클로저가 붙어서 괄호를 쓴다
    api(libs.bundles.smack) {
        exclude group: 'org.junit.jupiter'
        exclude group: 'org.mockito'
    }
}
```

모든 configuration 에서 한 번에 뺄 때는 `configurations.configureEach` 를 쓴다(oadr-server 의 spring-boot-starter-logging).

```groovy
configurations.configureEach {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
}
```

configuration 을 직접 만들 수도 있다. 선언용과 푸는 용을 나눈다.

- `configurations.dependencyScope('x')`: 의존성을 적는 자리. 스스로는 풀리지 않는다.
- `configurations.resolvable('y') { extendsFrom ... }`: 실제로 파일 목록으로 푸는 자리. 여기엔 의존성을 직접 적지 않는다.

```groovy
// oadr-client/OpenADRModel20b/build.gradle: XJC 를 돌릴 클래스패스
configurations.dependencyScope('xjc')
def xjcClasspath = configurations.resolvable('xjcClasspath') {
    extendsFrom configurations.bom, configurations.xjc
}

dependencies {
    xjc libs.jaxb.xjc          // 만든 configuration 이름이 그대로 메서드가 된다
}

tasks.register('xjc', JavaExec) {
    classpath = xjcClasspath.get()
}
```

mockito 에이전트(`mockitoAgent`, `mockitoAgentClasspath`)도 같은 방식이다. `transitive = false` 는 끌려오는 의존성 없이 그 jar 하나만 받는다.

## BOM(버전 맞추기)

버전이 없는 라이브러리는 Spring Boot BOM 값을 쓴다.

```groovy
configurations.dependencyScope('bom')
['compileClasspath', 'runtimeClasspath', 'testCompileClasspath', 'testRuntimeClasspath', 'annotationProcessor'].each {
    configurations.named(it) { extendsFrom configurations.bom }
}

dependencies {
    bom enforcedPlatform(catalog.spring.boot.bom)
}
```

- `platform(...)`: BOM 을 권장 버전으로 쓴다. 다른 의존성이 더 높은 버전을 원하면 그쪽이 이긴다.
- `enforcedPlatform(...)`: BOM 값으로 고정한다. 메이븐 dependencyManagement 와 같다. 이 저장소는 이걸 쓴다.
  `platform` 이었을 때 rabbitmq-jms 가 원하는 amqp-client 5.35.0 이 BOM 의 5.30.0 을 이기는 식으로 버전이 올라갔다.
- `bom` 을 따로 둔 이유: `implementation` 에 BOM 을 넣으면 oadr-client 를 배포할 때 pom 에 BOM 이 같이 실려서 쓰는 쪽에 강제된다.
  배포되지 않는 configuration 에 두고 클래스패스들이 이어 받게 했다. 배포 pom 의 버전은 `versionMapping` 이 실제로 고른 값으로 채운다.

## 태스크

```groovy
tasks.register('이름', 타입) { 설정 }     // 새로 만든다. 실제로 필요할 때만 설정이 돈다(지연)
tasks.named('이름', 타입) { 설정 }        // 있는 태스크를 고친다
tasks.withType(타입).configureEach { }  // 그 타입 태스크 전부
```

```groovy
// oadr-server/build.gradle: 모든 컴파일 태스크
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.compilerArgs.add('-parameters')
}

// 있는 test 태스크 고치기(타입을 주면 Test 의 속성을 바로 쓴다)
tasks.named('test', Test) {
    useJUnitPlatform()
    usesService(serialTests)
}
```

태스크 사이의 순서와 관계:

- `dependsOn x`: x 를 먼저 돌린다.
- `finalizedBy x`: 끝난 뒤 x 를 돌린다. 앞 태스크가 실패해도 돈다(루트 test 뒤 testReport).
- `mustRunAfter x`: 둘 다 돌 때만 순서를 정한다.
- `from(태스크)`: 복사 원본을 태스크 출력으로 주면 그 태스크에 대한 의존도 같이 생긴다(processResources 의 frontendBuild).

증분 빌드(입력이 안 바뀌면 건너뛰기)는 입력과 출력을 적어야 동작한다.

```groovy
// oadr-server/OpenADRServerVTN20b/build.gradle
def frontendBuild = tasks.register('frontendBuild', NpmTask) {
    description = 'React UI 를 빌드한다(npm run build, 결과는 frontend/build)'
    group = 'build'
    dependsOn tasks.named('npmInstall')
    args = ['run', 'build']
    inputs.dir('frontend/src').withPropertyName('src').withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir('frontend/build').withPropertyName('build')
    outputs.cacheIf { true }       // 빌드 캐시에 올려도 된다
}
```

- `doFirst { }`, `doLast { }`: 태스크가 실제로 돌 때 실행되는 코드. 블록 밖 코드는 설정 단계에 돈다.
  설정 단계에 파일을 지우거나 만드는 코드를 두면 태스크를 안 돌려도 실행된다. 그런 코드는 doFirst, doLast 안에 둔다.
- `group`, `description`: `./gradlew tasks` 와 IntelliJ Gradle 창에 보이는 분류와 설명.
- 자주 쓰는 타입: `JavaExec`(자바 main 실행, xjc), `Copy`, `Sync`(복사하고 없어진 파일은 지움, collectJars), `Delete`(cleanTarget), `TestReport`, `NpmTask`.
- doFirst, doLast 안에서 `project` 를 부르면(`project.delete` 같은 것) 그래들 10 에서 막힌다. 필요한 값은 설정 단계에 변수로 잡아 두거나 `Delete` 같은 태스크 타입을 쓴다.
  타입 없이 만든 태스크는 dependsOn 만 있는 묶음 태스크다(루트 build, test).

인자를 늦게 계산할 때는 `CommandLineArgumentProvider` 를 쓴다. 설정 단계엔 파일 목록이 없어도 되고, 돌 때 계산한다.

```groovy
jvmArgumentProviders.add({ ["-javaagent:${mockitoAgentClasspath.get().singleFile}".toString()] } as CommandLineArgumentProvider)
```

## 지연 값(Provider)과 경로

그래들 9 의 속성은 대부분 지연 값이다. 값을 넣을 때는 `=`, 꺼낼 때는 `.get()`, 변환은 `.map { }` 이다.

```groovy
def xjcJavaDir = layout.buildDirectory.dir('generated/sources/xjc/main')   // Provider<Directory>
outputs.dir(xjcJavaDir)                                                    // 그대로 넘긴다
xjcJavaDir.get().asFile.path                                               // 태스크가 돌 때 실제 경로로
def withFrontend = providers.gradleProperty('frontend').map { it != 'false' }.getOrElse(true)
```

- `layout.buildDirectory`: 모듈의 build 디렉토리. `buildDir` 는 없어진 옛 문법이다.
- `layout.projectDirectory`, `file('...')`: 모듈 디렉토리 기준 경로.
- `fileTree('xsd/oadr20b_schema') { include '**/*.xsd' }`: 패턴으로 고른 파일 묶음.
- `files(dir).builtBy(task)`: 이 경로는 그 태스크가 만든다고 알려 준다. sourceSets 에 생성 소스를 붙일 때 쓴다.

```groovy
sourceSets.main.java.srcDir(files(xjcJavaDir).builtBy(xjcTask))
sourceSets.main.resources.srcDir(files(xjcResourceDir).builtBy(xjcTask))
```

## 공통 설정 나누기

모듈 공통 설정은 빌드 루트 build.gradle 의 `subprojects { }` 에 둔다. 이 안의 코드는 모듈마다 한 번씩 돈다.

```groovy
subprojects {
    apply plugin: 'java-library'      // plugins 블록을 못 써서 apply plugin
    ...
    // 스프링 부트 플러그인이 붙은 모듈에서만
    plugins.withId('org.springframework.boot') {
        tasks.named('jar') {
            enabled = false
        }
    }
}
```

- `plugins.withId('id') { }`: 그 플러그인이 붙은 모듈에서만 돈다. 모듈이 나중에 플러그인을 붙여도 그때 돈다.
- 빌드 서비스는 여러 태스크가 나눠 쓰는 자원이다. `maxParallelUsages = 1` 이면 그 서비스를 쓰는 태스크는 한 번에 하나만 돈다
  (8182 포트를 같이 쓰는 VTN 테스트).

```groovy
abstract class SerialTestService implements BuildService<BuildServiceParameters.None> {}
def serialTests = gradle.sharedServices.registerIfAbsent('serialTests', SerialTestService) {
    maxParallelUsages = 1
}
```

- composite 루트에서 물린 빌드의 태스크는 `gradle.includedBuild('oadr-client').task(':build')` 로 가리킨다.
  빌드 이름은 디렉토리 이름이다. 태스크 경로 하나만 가리킬 수 있어서 oadr-client, oadr-server 빌드 루트에
  모듈 전체를 묶는 태스크(build, test 등)를 두었다.
- 명령줄에서 물린 빌드의 모듈은 `:빌드이름:모듈:태스크` 로 부른다(`./gradlew :oadr-server:OpenADRServerVTN20b:bootRun`).

## 프로퍼티(-P)

- `gradle.properties` 의 `org.gradle.*` 은 그래들 옵션, 나머지(group, version)는 프로젝트 값이다.
- 명령줄 `-P이름=값` 은 `providers.gradleProperty('이름')` 으로 읽는다. composite 루트에서 준 값은 물린 빌드에도 간다.

```
./gradlew build -Pfrontend=false
./gradlew build -PopenadrClientDir=../other/oadr-client
```

## 자주 쓰는 명령

```
./gradlew tasks                                   # 이 프로젝트 태스크 목록(--all 이면 전부)
./gradlew :oadr-server:OpenADRServerVTN20b:dependencies --configuration runtimeClasspath
./gradlew :oadr-server:OpenADRServerVTN20b:dependencyInsight --configuration runtimeClasspath --dependency amqp-client
./gradlew build --warning-mode all                # 없어질 문법 경고를 전부 본다
./gradlew test --continue                         # 실패해도 나머지 모듈까지
./gradlew :oadr-client:OpenADRSecurity:test --rerun    # 최신이어도 다시 돌린다
./gradlew build --scan                            # 빌드 스캔(외부 서버로 올라간다. 필요할 때만)
```

`dependencyInsight` 는 어떤 버전이 왜 골라졌는지(누가 끌고 왔는지, BOM 이 고정했는지) 보여 준다.

## 메이븐과 다른 점, 걸렸던 것

- 버전 충돌: 메이븐은 가까운 쪽이 이기고, 그래들은 높은 쪽이 이긴다. BOM 을 `enforcedPlatform` 으로 건 이유다.
- 스코프: 메이븐 runtime 스코프는 테스트 컴파일에도 들어가지만 그래들 runtimeOnly 는 안 들어간다.
  smack-xmlparser-stax 를 테스트에서 직접 쓰려고 `testImplementation` 으로 따로 넣었다.
- 메이븐 provided 는 테스트 클래스패스에도 들어간다. 그래들 compileOnly 는 안 들어가서 테스트용으로 따로 넣는다(VTNCommon 의 브로커).
- 메이븐에서 가려졌던 잘못된 의존성: smack 4.5.0 pom 의 compile 스코프 junit, mockito 는 그래들에서 그대로 끌려와서 exclude 했다.
- 그래들 9 는 JUnit 을 쓰면 `junit-platform-launcher` 를 `testRuntimeOnly` 로 적어야 한다.
- 모듈 build.gradle 에 저장소를 적는 플러그인(node-gradle 의 node 배포본)은 FAIL_ON_PROJECT_REPOS 와 부딪힌다.
  저장소를 settings.gradle 에 옮기고 플러그인 쪽은 끈다(`distBaseUrl = null`).
- 메이븐 jar 플러그인은 src/main/java 도 리소스로 넣는 설정이 있었다. 그래들 jar 에는 .java 가 안 들어가고 sources jar 가 따로 나온다.
