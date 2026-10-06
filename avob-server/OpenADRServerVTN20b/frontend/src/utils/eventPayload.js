import { signalsToICal } from './time'

// 이벤트 만들기(POST), 고치기(PUT)에 보낼 신호와 대상 모양.
// 서버 DTO(DemandResponseEventSignalDto, TargetDto)에 있는 필드만 보낸다.
// 화면용 값(unitType)과 읽어 올 때만 붙는 값은 뺀다

// 입력칸 값은 문자열이라 숫자 필드는 숫자로 보낸다. 비우면 null
export function numberOrNull(value) {
  return (value == null || value === "") ? null : Number(value);
}

// 서버 TargetTypeEnum 은 VEN, GROUP 이름으로만 읽는다. 소문자(ven, group)면 JSON 을 못 읽고 400 이다
export function targetsToPayload(targets) {
  return (targets || []).map((target) => ({
    targetType: String(target.targetType).toUpperCase(),
    targetId: target.targetId,
  }));
}

// 구간 길이는 화면에서 분이라 XML 기간(PT15M)으로 바꾼다(utils/time 의 signalsToICal).
// API 로 만든 이벤트에 있던 itemBase, 신호별 대상은 고칠 때 잃지 않게 그대로 둔다
export function signalsToPayload(signals) {
  return signalsToICal(signals).map((signal) => ({
    signalId: signal.signalId,
    signalName: signal.signalName,
    signalType: signal.signalType,
    currentValue: numberOrNull(signal.currentValue),
    intervals: signal.intervals.map((interval) => ({
      duration: interval.duration,
      value: numberOrNull(interval.value),
    })),
    itemBase: signal.itemBase ? signal.itemBase : undefined,
    targets: signal.targets ? targetsToPayload(signal.targets) : undefined,
  }));
}
