import React from 'react';
// 1.x 는 BigCalendar.Navigate 대신 Navigate 를 이름으로 내보낸다. 본체와 같은 lib 에서 가져온다(EventCalendar.jsx 참고)
import { Navigate } from 'react-big-calendar/lib'

import moment from 'moment'

import AgendaView from 'react-big-calendar/lib/Agenda'



// react-big-calendar 0.20 의 Agenda 는 클래스라 상속해서 static 메서드(range, navigate, title)만 바꿔 썼다.
// 1.x 에서 Agenda 가 함수 컴포넌트가 되면서 상속하면 React 가 클래스를 함수처럼 불러
// "Class constructor cannot be invoked without 'new'" 로 캘린더 화면 전체가 죽었다.
// 상속 대신 그대로 감싸고 static 메서드는 아래에서 이 함수에 붙인다
function EventCalendarAgendaView( props ) {
  return <AgendaView { ...props } />;
}

// var getRange = (date) => {
    
//     return {
//       start:start.toDate(),
//       end:end.toDate()
//     }

// }

EventCalendarAgendaView.range = date => {


  let start = moment(date).startOf('week');;
  let end = moment(start.toDate())
  end.add(7, 'day');

  let current = moment(start.toDate());
  let range = []

  while (current.toDate().getTime() < end.toDate().getTime()) {
    range.push(current.toDate())
    current.add(1, 'day')
  }

  return range;
}

EventCalendarAgendaView.navigate = (date, action) => {
  let d = moment(new Date(date));
  switch (action) {
    case Navigate.PREVIOUS:
      d.add(-7, 'day')
      return d.toDate();

    case Navigate.NEXT:
      d.add(7, 'day');
      return d.toDate();

    case Navigate.TODAY:
      date = new Date();
     
      return date;

    default:
      return date
  }
}



EventCalendarAgendaView.title = date => {
  let start = moment(date).startOf('week');;
  let end = moment(start.toDate())
  end.add(7, 'day');
  return (
    <span>
      <span style={{paddingTop:10}}>{start.format( "M/D/YYYY")} - {end.format( "M/D/YYYY")}</span>
      <span className="rbc-btn-group" style={{float:"right"}} >
        <button type="button" className={(EventCalendarAgendaView.color === "status") ? "rbc-active" : ""} onClick={(e) => {EventCalendarAgendaView.onColorChange("status")}}>Color Status</button>
        <button type="button" className={(EventCalendarAgendaView.color  === "market") ? "rbc-active" : ""} onClick={(e) => {EventCalendarAgendaView.onColorChange("market")}}>Color Market</button>
      </span>  
    </span>
    );
}

export default EventCalendarAgendaView;