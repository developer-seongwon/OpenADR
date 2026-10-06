import React from 'react';
// 1.x 는 BigCalendar.Navigate 대신 Navigate 를 이름으로 내보낸다. 본체와 같은 lib 에서 가져온다(EventCalendar.jsx 참고)
import { Navigate } from 'react-big-calendar/lib'

import TimeGrid from 'react-big-calendar/lib/TimeGrid'
import { timeGridDefaults } from './timeGridDefaults'
// dayjs 는 불변이라 add() 결과를 다시 받아야 한다(utils/dayjs.js 참고)
import dayjs from '../../../utils/dayjs'


class EventCalendarDayView extends React.Component {
  render() {
    let { date } = this.props
    let range = EventCalendarDayView.range(date)

    return <TimeGrid {...this.props} {...timeGridDefaults(this.props)} range={range} eventOffset={10} />
  }
}

// var getRange = (date) => {
   
//     return {
//       start:start.toDate(),
//       end:end.toDate()
//     }

// }

EventCalendarDayView.range = date => {
  return [dayjs(date).startOf('day').toDate()];
}

EventCalendarDayView.navigate = (date, action) => {
  let d = dayjs(date);
  switch (action) {
    case Navigate.PREVIOUS:
      return d.add(-1, 'day').toDate();

    case Navigate.NEXT:
      return d.add(1, 'day').toDate();

    case Navigate.TODAY:
      date = new Date();     
      return date;

    default:
      return date
  }
}



EventCalendarDayView.title = date => {
  return (
    <span>
      <span style={{paddingTop:10}}>{dayjs(date).format( "dddd MMM DD")}</span>
      <span className="rbc-btn-group" style={{float:"right"}} >
        <button type="button" className={(EventCalendarDayView.color === "status") ? "rbc-active" : ""} onClick={(e) => {EventCalendarDayView.onColorChange("status")}}>Color Status</button>
        <button type="button" className={(EventCalendarDayView.color  === "market") ? "rbc-active" : ""} onClick={(e) => {EventCalendarDayView.onColorChange("market")}}>Color Market</button>
      </span>  
    </span>
    );
}

export default EventCalendarDayView;