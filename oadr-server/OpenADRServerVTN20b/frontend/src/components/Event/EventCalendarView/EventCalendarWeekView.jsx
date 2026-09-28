import React from 'react';
// 1.x 는 BigCalendar.Navigate 대신 Navigate 를 이름으로 내보낸다. 본체와 같은 lib 에서 가져온다(EventCalendar.jsx 참고)
import { Navigate } from 'react-big-calendar/lib'

import TimeGrid from 'react-big-calendar/lib/TimeGrid'
import { timeGridDefaults } from './timeGridDefaults'
// dayjs 는 불변이라 add() 결과를 다시 받아야 한다(utils/dayjs.js 참고)
import dayjs from '../../../utils/dayjs'


class EventCalendarWeekView extends React.Component {
  render() {
    let { date } = this.props
    let range = EventCalendarWeekView.range(date)

    return <TimeGrid {...this.props} {...timeGridDefaults(this.props)} range={range} eventOffset={15} />
  }
}

// var getRange = (date) => {
   
//     return {
//       start:start.toDate(),
//       end:end.toDate()
//     }

// }

EventCalendarWeekView.range = date => {


  let start = dayjs(date).startOf('week');
  let end = start.add(7, 'day');

  let current = start;
  let range = []

  while (current.isBefore(end)) {
    range.push(current.toDate())
    current = current.add(1, 'day')
  }

  return range;
}

EventCalendarWeekView.navigate = (date, action) => {
  let d = dayjs(date);
  switch (action) {
    case Navigate.PREVIOUS:
      return d.add(-7, 'day').toDate();

    case Navigate.NEXT:
      return d.add(7, 'day').toDate();

    case Navigate.TODAY:
    console.log("today")
      date = new Date();
     
      return date;

    default:
      return date
  }
}


EventCalendarWeekView.title = date => {
  let start = dayjs(date).startOf('week');
  let end = start.add(7, 'day');
  return (
    <span>
      <span style={{paddingTop:10}}>{start.format( "MMM Do")} - {end.format( "MMM Do")}</span>
      <span className="rbc-btn-group" style={{float:"right"}} >
          <button type="button" className={(EventCalendarWeekView.color === "status") ? "rbc-active" : ""} onClick={(e) => {EventCalendarWeekView.onColorChange("status")}}>Color Status</button>
          <button type="button" className={(EventCalendarWeekView.color  === "market") ? "rbc-active" : ""} onClick={(e) => {EventCalendarWeekView.onColorChange("market")}}>Color Market</button>
        </span>  
    </span>
    );
}

export default EventCalendarWeekView;