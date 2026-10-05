import pxd from 'parse-xsd-duration'

//
// Takes an ical duration in a outputs it as a total # of seconds
//
export function iCalDurationInSeconds(durStr) {
  return pxd(durStr)
}

//
// Take total of minutes and output ical duration
//
export function minutesToICalDuration(minutes) {
  return "PT"+minutes+"M";
}

var enforceTwoDigit = (date) => {
  date = ""+date;
  if(date.length === 1){
    date = "0"+ date;
  }
  return date;
} 

// 브라우저 시간대 기준으로 보여 준다. 예전에는 tz 를 "UTC" 로 적어서 한국 시각을 UTC 라고 써 붙였다
export function formatTimestamp(timestamp) {
  var date = new Date();
  date.setTime(timestamp);
  return {
    date: date.getFullYear() + "-" + enforceTwoDigit(date.getMonth()+1) + "-" + enforceTwoDigit(date.getDate()),
    time: enforceTwoDigit(date.getHours()) + ":" + enforceTwoDigit(date.getMinutes()),
    tz: browserTimezone()
  }

}

// 브라우저 시간대(Asia/Seoul 같은 IANA 이름). 못 읽으면 UTC
export function browserTimezone() {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
  } catch (e) {
    return 'UTC';
  }
}

// 그 시간대의 벽시계 값(year, month, day, hour, minute, second 문자열). 모르는 시간대면 브라우저 시간대로 본다
function zonedParts(timestamp, tz) {
  var options = {
    hourCycle: 'h23', year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  };
  var format;
  try {
    format = new Intl.DateTimeFormat('en-US', { ...options, timeZone: tz || browserTimezone() });
  } catch (e) {
    format = new Intl.DateTimeFormat('en-US', options);
  }
  var parts = {};
  format.formatToParts(new Date(timestamp)).forEach((part) => {
    parts[part.type] = part.value;
  });
  // 오래된 브라우저는 자정을 24 로 준다
  if (parts.hour === '24') {
    parts.hour = '00';
  }
  return parts;
}

// 타임스탬프를 그 시간대의 날짜(YYYY-MM-DD), 시각(HH:mm)으로. 날짜, 시각 입력칸에 넣을 값이다
export function timestampToZoned(timestamp, tz) {
  var parts = zonedParts(timestamp, tz);
  return { date: parts.year + '-' + parts.month + '-' + parts.day, time: parts.hour + ':' + parts.minute };
}

// timestamp 순간에 그 시간대가 UTC 보다 앞선 만큼(ms). 서울이면 9시간
function zoneOffset(timestamp, tz) {
  var parts = zonedParts(timestamp, tz);
  var asUtc = Date.UTC(Number(parts.year), Number(parts.month) - 1, Number(parts.day),
    Number(parts.hour), Number(parts.minute), Number(parts.second));
  return asUtc - Math.floor(timestamp / 1000) * 1000;
}

// 그 시간대의 날짜(YYYY-MM-DD), 시각(HH:mm)을 타임스탬프로.
// 서머타임이 바뀌는 날은 처음 구한 차이가 틀릴 수 있어 한 번 더 맞춘다. 날짜가 비었으면 null
export function zonedToTimestamp(date, time, tz) {
  if (!date) {
    return null;
  }
  var d = String(date).split('-').map(Number);
  var t = String(time || '00:00').split(':').map(Number);
  var guess = Date.UTC(d[0], d[1] - 1, d[2], t[0] || 0, t[1] || 0);
  if (isNaN(guess)) {
    return null;
  }
  var offset = zoneOffset(guess, tz);
  var result = guess - offset;
  var corrected = zoneOffset(result, tz);
  return corrected === offset ? result : guess - corrected;
}
//
// 신호 구간 길이. 화면에서는 분(숫자)으로 다루고 서버와는 ISO-8601 기간(PT15M)으로 주고받는다.
// 예전에는 화면이 분 숫자("3")를 그대로 보내서 VTN 이 이벤트를 VEN 에 보낼 때 XML 기간으로 읽지 못했다
// (Oadr20bVTNEiEventService 가 구간 길이를 그대로 xcal:duration 으로 쓴다).
// 그때 저장된 숫자 값도 분으로 읽는다. 못 읽으면 빈 문자열
//
export function durationToMinutes(duration) {
  if (duration == null || duration === '') {
    return '';
  }
  var text = String(duration).trim();
  if (/^\d+(\.\d+)?$/.test(text)) {
    return Number(text);
  }
  try {
    var seconds = iCalDurationInSeconds(text);
    return (typeof seconds === 'number' && !isNaN(seconds)) ? seconds / 60 : '';
  } catch (e) {
    return '';
  }
}

// 분을 XML 기간으로. 정수 분은 PT15M, 소수는 초로 바꾼다(XML 기간은 초에만 소수를 쓸 수 있다)
export function minutesToIntervalDuration(minutes) {
  var value = Number(minutes);
  if (Number.isInteger(value)) {
    return minutesToICalDuration(value);
  }
  return "PT" + Math.round(value * 60) + "S";
}

// 서버에서 받은 신호 목록을 화면용(구간 길이는 분)으로. 리덕스 상태를 건드리지 않게 복사한다
export function signalsToMinutes(signals) {
  return (signals || []).map((signal) => ({
    ...signal,
    intervals: (signal.intervals || []).map((interval) => ({ ...interval, duration: durationToMinutes(interval.duration) })),
  }));
}

// 화면의 신호 목록을 서버로 보낼 모양(구간 길이는 XML 기간)으로
export function signalsToICal(signals) {
  return (signals || []).map((signal) => ({
    ...signal,
    intervals: (signal.intervals || []).map((interval) => ({
      ...interval,
      duration: minutesToIntervalDuration(interval.duration),
    })),
  }));
}
