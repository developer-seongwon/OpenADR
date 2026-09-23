import React from 'react';
// 1.x 는 BigCalendar.Navigate 대신 Navigate 를 이름으로 내보낸다. 본체와 같은 lib 에서 가져온다(EventCalendar.jsx 참고)
import { Navigate } from 'react-big-calendar/lib'

import TimeGrid from 'react-big-calendar/lib/TimeGrid'
import { timeGridDefaults } from './timeGridDefaults'
import moment from 'moment'


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
  return [moment(date).startOf('day').toDate()];
}

EventCalendarDayView.navigate = (date, action) => {
  let d = moment(new Date(date));
  switch (action) {
    case Navigate.PREVIOUS:
      d.add(-1, 'day')
      return d.toDate();

    case Navigate.NEXT:
      d.add(1, 'day');
      return d.toDate();

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
      <span style={{paddingTop:10}}>{moment(date).format( "dddd MMM DD")}</span>
      <span className="rbc-btn-group" style={{float:"right"}} >
        <button type="button" className={(EventCalendarDayView.color === "status") ? "rbc-active" : ""} onClick={(e) => {EventCalendarDayView.onColorChange("status")}}>Color Status</button>
        <button type="button" className={(EventCalendarDayView.color  === "market") ? "rbc-active" : ""} onClick={(e) => {EventCalendarDayView.onColorChange("market")}}>Color Market</button>
      </span>  
    </span>
    );
}

export default EventCalendarDayView;