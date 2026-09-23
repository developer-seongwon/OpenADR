// react-big-calendar 0.20 은 Calendar 가 min, max, scrollToTime 기본값을 채워서 뷰에 넘겼다.
// 1.x 부터는 Calendar 가 안 채우고 Week, Day 같은 내장 뷰가 TimeGrid 를 그리기 직전에 채운다
// (lib/Week.js 의 render 참고).
// 우리 주간, 일간 뷰는 내장 뷰 대신 TimeGrid 를 직접 그려서 이 기본값이 빠졌고,
// 그러면 시간 눈금이 사라지고 이벤트가 맨 위 한 줄에 몰려 그려졌다. 내장 뷰와 같은 값을 채운다
export function timeGridDefaults( props ) {
  const { localizer, min, max, scrollToTime, enableAutoScroll } = props;
  return {
    min: min === undefined ? localizer.startOf( new Date(), 'day' ) : min,
    max: max === undefined ? localizer.endOf( new Date(), 'day' ) : max,
    scrollToTime: scrollToTime === undefined ? localizer.startOf( new Date(), 'day' ) : scrollToTime,
    enableAutoScroll: enableAutoScroll === undefined ? true : enableAutoScroll,
  };
}
