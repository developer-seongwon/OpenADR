// moment 대신 dayjs 를 쓴다. moment 는 2020 년부터 유지보수 모드라 새 기능이 없고 번들도 크다.
// dayjs 는 API 가 moment 와 거의 같지만 불변(immutable)이다.
// moment 처럼 d.add(1, 'day') 만 부르면 d 는 그대로라서, 반드시 d = d.add(1, 'day') 처럼 결과를 다시 받아야 한다.
//
// 플러그인은 dayjs 전역에 한 번만 붙이면 되니 여기서 붙이고, 날짜를 다루는 파일은 이 모듈을 import 한다.
// react-big-calendar 의 dayjsLocalizer 는 자기가 필요한 플러그인(isBetween, localeData, utc 등)을 스스로 붙인다
import dayjs from 'dayjs'
// "MMM Do" 의 Do(1st, 2nd ...) 는 advancedFormat 플러그인이 있어야 나온다. 없으면 "Do" 글자가 그대로 찍힌다
import advancedFormat from 'dayjs/plugin/advancedFormat'

dayjs.extend(advancedFormat)

export default dayjs
