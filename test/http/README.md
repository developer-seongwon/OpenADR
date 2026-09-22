# test/http

VTN 을 손으로 두드려 보는 HTTP 요청 모음이다.
IntelliJ 의 HTTP Client 로 열어서 요청 옆의 실행 버튼을 누르면 된다.

구성은 OpenADR 2.0b Profile Specification v1.1 의 서비스 순서를 따랐다.
각 요청 위 주석에 해당 스펙 절을 적어 뒀다.

## 준비

스택을 띄운다.

```
./docker/run.sh start all
```

환경은 `http-client.env.json` 의 `local` 을 고른다. IntelliJ 오른쪽 위 드롭다운에 나온다.

자체 서명 인증서라 TLS 검증을 꺼야 한다. `http-client.private.env.json` 에
`SSLConfiguration.verifyHostCertificate: false` 를 넣어 뒀는데, IDE 버전에 따라
키 이름이 다를 수 있다. 붙지 않으면 Settings 의 Tools > HTTP Client 에서
`localhost` 를 신뢰하도록 잡아라.

## 파일

```
00-vtn-setup.http    준비. 마켓컨텍스트, 그룹, VEN 계정, 리소스 (관리 API)
01-registration.http EiRegisterParty  등록
02-report.http       EiReport         리포트 명세, 요청, 데이터, 취소
03-event.http        EiEvent          이벤트 배포와 응답
04-opt.http          EiOpt            opt 스케줄
05-poll.http         OadrPoll         폴링
06-vtn-admin.http    나머지 관리 API
99-cleanup.http      정리
payloads/            긴 XML 본문
```

00 과 06 은 프로토콜이 아니라 웹 화면이 쓰는 REST API 다.
00 은 프로토콜을 시작하려면 먼저 있어야 하는 것들이고, 06 은 나머지를 모은 것이다.

01 부터 05 가 실제 OpenADR 프로토콜이다. 엔드포인트는 `/OpenADR2/Simple/2.0b/<서비스>`
이고 본문은 XML 이다.

## 시나리오

위에서 아래로 그냥 실행하면 된다. 순서에 이유가 있다.

VEN 이 VTN 에 붙는 전체 흐름은 스펙 기준으로 이렇다.

먼저 신원이 잡혀 있어야 한다. OpenADR 은 통신 전에 양쪽이 서로를 아는 걸 전제로 한다
(스펙 10.5 System Registration). 여기서는 `00-vtn-setup.http` 에서 VEN 계정을 만드는 게
그 단계다. 프로토콜의 party registration 과는 다른 것이다.

그다음이 등록이다(스펙 8.4). VEN 이 `oadrQueryRegistration` 으로 VTN 이 뭘 지원하는지
먼저 물어볼 수 있고, 이건 선택이다. 그러고 나서 `oadrCreatePartyRegistration` 을 보내면
VTN 이 `registrationID` 와 폴링 주기를 돌려준다. 등록은 언제나 VEN 이 시작한다.

등록이 끝나야 리포트로 갈 수 있다(스펙 8.3.2.1 의 "after completion of party registration").
VEN 이 `oadrRegisterReport` 로 자기가 줄 수 있는 것의 명세(METADATA)를 올리고,
VTN 이 그중에서 원하는 걸 `oadrCreateReport` 로 요청하고,
VEN 이 `oadrCreatedReport` 로 받았다고 답한 다음,
`oadrUpdateReport` 로 실제 값을 올린다. 이 마지막 단계가 텔레메트리 본체다.

이벤트는 언제나 VTN 이 만든다(스펙 8.1). pull VEN 은 `oadrPoll` 이나
`oadrRequestEvent` 로 `oadrDistributeEvent` 를 가져가고,
`responseRequired` 가 always 면 `oadrCreatedEvent` 로 참여 여부를 답해야 한다.

opt 는 이벤트 하나에 대한 답이 아니라 기간에 대한 것이다(스펙 8.5).
"이 시간대에는 참여 안 한다" 를 미리 알려 두는 쪽이다.

`### [정리]` 로 시작하는 요청은 만든 걸 되돌리는 것이다. 다 돌린 다음에 실행해라.

## 화면에서 어디를 보나

각 요청 바로 아래에 `# 확인:` 으로 그 결과가 화면 어디에 나오는지 적어 뒀다.
웹 UI 는 https://localhost:8181/testvtn/ 이고 `admin` / `admin` 이다.

큰 틀은 이렇다.

VEN 에 관한 것은 전부 VENs 목록에서 그 VEN 을 누르면 나오는 탭들에 있다.
Settings 는 등록 상태와 Registration ID, Reports 는 VEN 이 올린 리포트 명세,
Requests 는 VTN 이 요청해 둔 리포트, OptSchedules 는 opt,
Enrollments 는 리소스와 마켓컨텍스트, Groups 는 그룹이다.

이벤트는 Events 목록에서 그 이벤트를 누르면 Descriptor, Active Period, Signals,
Targets, Ven Responses 탭이 나온다. VEN 이 참여 응답을 보냈는지는 Ven Responses 에서 본다.

마켓컨텍스트와 그룹은 VTN Config, 계정과 앱은 Accounts 다.

### 리포트로 올린 데이터

이게 제일 찾기 어려운 자리라 따로 적는다.

```
VENs > 그 VEN > Requests 탭 > 요청 줄을 클릭
```

그러면 rid 별로 Archived, Last Update Date/Time, Last Update Value 가 나온다.
`02-report.http` 의 2-9 에서 올린 값이 여기 뜬다.
주소로 바로 가려면 `/ven/detail/{venId}/reports/{reportSpecifierId}/requests/{reportRequestId}` 다.

**화면에 나오는 건 rid 별 마지막 값 하나뿐이다.** 쌓인 이력 전체를 보는 화면은 없다.
그건 `GET /Ven/{venID}/report/data/float/{reportSpecifierId}` 로만 볼 수 있고,
프론트엔드는 그 엔드포인트를 아예 부르지 않는다.
이력까지 화면에서 보고 싶으면 그 탭에 목록이나 그래프를 새로 붙여야 한다.

### 화면에 안 나오는 것들

프로토콜 요청 중 VEN 이 보내는 답(`oadrRegisteredReport`, `oadrCreatedReport`,
`oadrCanceledReport`, `oadrResponse`)과 `oadrPoll` 은 화면에 흔적을 남기지 않는다.
VEN 쪽 동작이라 그렇다. 응답 XML 로 확인한다.

VTN 이 VEN 에게 거는 것(재등록 요청, 리포트 명세 전송, 해지)은 큐에 들어갈 뿐이라
그 자체로는 화면에 안 나온다. `05-poll.http` 의 `oadrPoll` 로 받아 봐야 확인된다.

## pull 과 push

VTN 이 VEN 에게 먼저 보내고 싶은 게 있어도, pull VEN 은 주소가 없어서 받을 길이 없다.
그래서 VEN 이 주기적으로 `oadrPoll` 로 물어본다(스펙 8.6).

이 파일들의 VEN 은 `oadrHttpPullModel` 이 true 인 pull VEN 이다.
그래서 VTN 쪽에서 시작하는 동작은 전부 이 모양이 된다.

관리 API 로 VTN 쪽에 무언가를 시킨다. 그러면 그게 큐에 들어간다.
`oadrPoll` 을 보내면 큐에서 하나가 나온다. VEN 은 그에 맞는 답을 별도 요청으로 보낸다.

한 번의 `oadrPoll` 에는 페이로드가 하나만 온다. 여러 개가 쌓여 있으면 계속 폴링해서
비워야 한다. 원하는 게 안 나오면 몇 번 더 눌러라. 비면 `oadrResponse` 가 온다.

dummy VEN 네 대가 같은 큐를 계속 쓰고 있고 DummyDRProgram 이 주기적으로 이벤트를
만들기 때문에, 이벤트가 먼저 튀어나오는 경우가 흔하다.

## 인증

관리 API 는 `admin` / `admin` basic 인증이다. 브라우저 화면이 쓰는 것과 같다.

프로토콜 엔드포인트는 원래 클라이언트 인증서로 붙는다. dummy VEN 들이 그렇게 붙어 있다.
그런데 `00-vtn-setup.http` 에서 만드는 VEN 은 `authenticationType` 이 `login` 이라
아이디와 비밀번호로도 붙는다. 그래서 인증서 설정 없이 프로토콜을 두드릴 수 있다.

페이로드의 `venID` 는 인증에 쓴 아이디와 같아야 한다. 다르면 462 TARGET_MISMATCH 다.

## 오류를 읽는 법

OpenADR 은 HTTP 상태와 응답 코드가 따로 논다. 요청이 문법적으로 멀쩡하면
HTTP 는 200 이 오고, 실제 결과는 본문의 `responseCode` 에 들어 있다.
그래서 200 만 보고 성공이라고 판단하면 안 된다.

자주 보는 것들이다.

`453 NOT_RECOGNIZED` 는 페이로드를 못 읽었다는 뜻이다. XML 이 깨졌거나
네임스페이스 선언이 빠졌을 때 나온다. `ei:schemaVersion` 을 쓰면서 `xmlns:ei` 를
안 적으면 여기로 떨어진다.

`454 INVALID_DATA` 는 스키마 검증 실패다. 필수 요소가 빠졌거나 순서가 틀렸을 때다.
`oadrCreateOpt` 가 대표적으로 까다롭다.

`462 TARGET_MISMATCH` 는 페이로드의 `venID` 가 인증 아이디와 다를 때다.

`452 INVALID_ID` 는 `registrationID` 같은 식별자가 서버가 아는 것과 다를 때다.

## 알아둘 것

`activePeriod.start` 는 이벤트 생성 시 필수다. 없으면 400 이 나는데 응답 본문에
이유가 없어서 서버 로그를 봐야 안다. `{{$timestamp}}000` 으로 현재 시각을 밀리초로 넣는다.

리포트 데이터가 DB 에 쌓이려면 `oadrUpdateReport` 의 `reportRequestID` 가
구독으로 만든 요청과 같아야 하고, `rID` 가 그 요청에 들어 있어야 하고,
그 rid 의 `archived` 가 true 여야 한다. 하나라도 어긋나면 200 은 오는데
아무것도 안 남는다. `02-report.http` 는 `reportRequestId` 를 고정값으로 주기 때문에
이 셋이 자동으로 맞는다.

`oadrCreateOpt` 의 `vavailability` 는 `xcal` 네임스페이스이고
`components > available > properties > dtstart/duration` 까지 다 있어야 한다.
요소 순서도 스키마 순서를 지켜야 한다.

이벤트 검색 필터의 `type` 은 `DemandResponseEventFilterType` 에 있는 것만 쓸 수 있다.
`VEN`, `MARKET_CONTEXT`, `EVENT_STATE`, `EVENT_PUBLISHED`, `EVENT_SENDABLE` 이다.
VEN 검색은 `VenFilterType` 이고 `EVENT`, `VEN`, `MARKET_CONTEXT`, `GROUP`, `IS_REGISTERED` 다.

계정 권한은 `VTNRoleEnum` 에 있는 것만 쓸 수 있다.
`ROLE_ADMIN`, `ROLE_DEVICE_MANAGER`, `ROLE_DRPROGRAM`, `ROLE_USER`, `ROLE_APP`,
`ROLE_VEN`, `ROLE_VTN` 이다.

## 업스트림에서 고친 것

여기 오면서 같이 고친 것들이다. 예전 동작을 기억하고 있다면 참고해라.

VEN 삭제가 500 이 나던 문제. `VenService.delete` 가 `venresource` 와
`vendemandresponseevent` 만 지우고 VTN20b 의 리포트 테이블을 남겨서 FK 제약에
걸렸다. 리포트 명세를 한 번이라도 올린 VEN 은 API 로도 화면으로도 못 지웠다.
`VenDeleteHandler` 라는 훅을 만들어서 각 모듈이 자기 뒷정리를 등록하게 했고,
VTN20b 의 `Oadr20bVenDeleteHandler` 가 리포트 명세와 요청, opt, 쌓인 데이터를
먼저 치운다.

등록되지 않은 `reportSpecifierId` 로 구독이나 요청을 걸면 406 이 온다.
예전에는 capability 가 null 인 채로 저장해서 그 VEN 의 구독 기능 전체를
망가뜨렸다.

구독에 `reportRequestId` 를 안 주면 서버가 UUID 로 만들어 준다.
예전에는 null 로 저장해서, 화면에서 건 구독은 VEN 이 데이터를 올려도
매칭이 안 돼 한 건도 안 쌓였다.

## 전부 한 번에 돌려보기

파일에 적힌 요청이 실제로 사는지 확인하는 러너를 같이 뒀다.
IntelliJ 를 대신하는 게 아니라, 요청이 죽어 있지 않은지만 본다.

```
cd test/http
bash .preclean.sh     # 지난 실행이 남긴 것 정리
python3 .runner.py
```

`### [정리]` 요청은 나머지를 다 돌린 다음에 실행한다.
`# @expect 403` 은 그 HTTP 상태가 나와야 통과, `# @expect-rc 462` 는
본문의 `responseCode` 가 그 값이어야 통과라는 뜻이다.
현재 99개 전부 통과한다.

## DB 를 비우고 싶을 때

이 시나리오가 만든 `httpven` 만 치우려면 `.preclean.sh` 를 돌리면 된다.
서버는 그대로 두고 다시 처음부터 돌릴 수 있다.

DB 를 통째로 비우고 싶으면 아래 둘 중 하나다. 어느 쪽이든 dummy 들도 다시 등록된다.

```
./docker/run.sh restart all    # 인프라까지 올리는 기동은 항상 비우고 시작한다
./docker/run.sh reset-db       # DB 만 비우고 끝
```

반대로 전체를 올리면서 DB 는 남기고 싶으면 `KEEP_DB=1 ./docker/run.sh restart all` 이다.
