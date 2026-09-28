import React from 'react';
// 1.x 는 BigCalendar.Navigate 대신 Navigate 를 이름으로 내보낸다. 본체와 같은 lib 에서 가져온다(EventCalendar.jsx 참고)
import { Navigate } from 'react-big-calendar/lib'

import MonthView from 'react-big-calendar/lib/Month'
// dayjs 는 불변이라 add() 결과를 다시 받아야 한다(utils/dayjs.js 참고)
import dayjs from '../../../utils/dayjs'


class EventCalendarMonthView extends MonthView {

}

// var getRange = (date) => {
   
//     return {
//       start:start.toDate(),
//       end:end.toDate()
//     }

// }

EventCalendarMonthView.range = date => {


  let start = dayjs(date).startOf('month');
  let end = dayjs(date).endOf('month');

  let current = start;
  let range = []

  while (current.isBefore(end)) {
    range.push(current.toDate())
    current = current.add(1, 'day')
  }

  return range;
}

EventCalendarMonthView.navigate = (date, action) => {
  let d = dayjs(date);
  switch (action) {
    case Navigate.PREVIOUS:
      return d.add(-1, 'month').toDate();

    case Navigate.NEXT:
      return d.add(1, 'month').toDate();

    case Navigate.TODAY:
      date = new Date();     
      return date;

    default:
      return date
  }
}



EventCalendarMonthView.title = date => {
  return (
    <span>
      <span style={{paddingTop:10}}>{dayjs(date).format( "YYYY MMMM")}</span>
      <span className="rbc-btn-group" style={{float:"right"}} >
        <button type="button" className={(EventCalendarMonthView.color === "status") ? "rbc-active" : ""} onClick={(e) => {EventCalendarMonthView.onColorChange("status")}}>Color Status</button>
        <button type="button" className={(EventCalendarMonthView.color  === "market") ? "rbc-active" : ""} onClick={(e) => {EventCalendarMonthView.onColorChange("market")}}>Color Market</button>
      </span>   
    </span>
    );
}

export default EventCalendarMonthView;