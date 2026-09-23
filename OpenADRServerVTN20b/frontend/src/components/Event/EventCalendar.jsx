import React from 'react';


// react-big-calendar 1.x 는 BigCalendar default export 를 없애고 Calendar, momentLocalizer 를 이름으로 내보낸다.
// 커스텀 뷰들이 lib/TimeGrid 같은 내부 파일(CommonJS)을 직접 쓰기 때문에, 본체도 같은 lib 에서 가져와야
// ESM 번들(dist)과 CommonJS(lib) 두 벌이 섞여 내부 모듈이 갈라지지 않는다
import { Calendar, momentLocalizer } from 'react-big-calendar/lib'
import moment from 'moment'
import Divider from '@mui/material/Divider';
// required for react-big-calendar
// 웹팩은 CSS 를 default import 로 받아도 넘어갔지만 Vite 는 CSS 에 default export 가 없다고 막는다.
// 받은 값은 어디서도 안 썼으니 스타일만 입히는 import 로 바꾼다
import 'react-big-calendar/lib/css/react-big-calendar.css'

import {iCalDurationInSeconds} from '../../utils/time'

import { EventCalendarDialog } from '../common/VtnconfigurationDialog'

import { history } from '../../store/configureStore';

import EventHeader from './EventHeader'

import EventCalendarWeekView from './EventCalendarView/EventCalendarWeekView'
import EventCalendarDayView from './EventCalendarView/EventCalendarDayView'
import EventCalendarMonthView from './EventCalendarView/EventCalendarMonthView'
import EventCalendarAgendaView from './EventCalendarView/EventCalendarAgendaView'


import { amber, red, green } from '@mui/material/colors';


const localizer = momentLocalizer(moment)


var marketContextColorCache = {};
export class EventCalendar extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
    this.state.eventCalendarDialog = false;
    this.state.selectedEvent = null;
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    marketContextColorCache = {};
    for(var i in this.props.marketContext) {
      var context = this.props.marketContext[i];
      marketContextColorCache[context.name] = context.color;
    }
  }

  handleEventCalendarDialogClose = () =>  {
    this.setState({eventCalendarDialog: false});
  }

  handleEditEvent = (id) => () => {
    history.push( '/event/detail/'+ id);
  }

  getEventPropGetter = (event, start, end, isSelected) => {
    if(this.props.color === "status") {
      var colorIntensity = 700;
      if(isSelected) {
        colorIntensity = 500;
      }

      var color;
      if(!event.published) {
        color = amber[colorIntensity];
      }
      else if(event.state === "ACTIVE"){
        color = green[colorIntensity];
      }
      else if(event.state === "CANCELLED") {
        color = red[colorIntensity];
      }

      
      return {
        style: {backgroundColor: color}
      }
    }
    else {
      return {
        style: {backgroundColor: marketContextColorCache[event.marketContext]}
      }
    }
      
  }

  handleSelectEvent = (event) => {
    this.setState({eventCalendarDialog: true, selectedEvent:event});
  }

  handleEventDetailClick = (id) => () => {
    history.push( '/event/detail/'+ id);
  }



  render() {
    var that = this;
    const {classes, marketContext, event, ven, filters, pagination, onFilterChange, onPaginationChange
      , onVenSuggestionsFetchRequested, onVenSuggestionsClearRequested, onVenSuggestionsSelect} = this.props;

    var calendarEvent = [];
    event.forEach(e => {
      var start = new Date();
      start.setTime(e.activePeriod.start);
      var end = new Date();
      end.setTime(e.activePeriod.start + iCalDurationInSeconds(e.activePeriod.duration) * 1000);
      calendarEvent.push({
        title:e.descriptor.marketContext+":"+e.id,
        start: start,
        end: end,
        allday:false,
        state: e.descriptor.state,
        published: e.published,
        marketContext:e.descriptor.marketContext,
        id:e.id
      });
    });


    EventCalendarWeekView.onColorChange = this.props.onColorChange;
    EventCalendarWeekView.color = this.props.color;
    EventCalendarDayView.onColorChange = this.props.onColorChange;
    EventCalendarDayView.color = this.props.color;
    EventCalendarMonthView.onColorChange = this.props.onColorChange;
    EventCalendarMonthView.color = this.props.color;
    EventCalendarAgendaView.onColorChange = this.props.onColorChange;
    EventCalendarAgendaView.color = this.props.color;

    
    


    return (
      <div className={ classes.root }>
        <EventHeader classes={classes}  marketContext={marketContext} event={event}
        filters={filters} pagination={pagination} onFilterChange={onFilterChange} onPaginationChange={onPaginationChange}
        ven={ven}
                onVenSuggestionsFetchRequested={onVenSuggestionsFetchRequested}
                onVenSuggestionsClearRequested={onVenSuggestionsClearRequested}
                onVenSuggestionsSelect={onVenSuggestionsSelect}/>
        <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
        <Calendar style={{height:600}}
          localizer={localizer}
          events={calendarEvent}
          defaultView={this.props.view}
          defaultDate={this.props.currentDate}
          startAccessor="start"
          endAccessor="end"
          eventPropGetter={this.getEventPropGetter}
          onSelectEvent={this.handleSelectEvent}
          views={{ month: EventCalendarMonthView, week: EventCalendarWeekView , day: EventCalendarDayView, agenda:EventCalendarAgendaView}}
          onNavigate={(date) => {
            that.props.onCurrentDateChange(date);
          }}
          onView={(view) => {
            that.props.onViewChange(view)
            }
          }
        />

        {(this.state.selectedEvent) ? <EventCalendarDialog classes={classes} title={(this.state.selectedEvent) ? this.state.selectedEvent.title : ""}
        event={this.state.selectedEvent}
        open={this.state.eventCalendarDialog}
        close={this.handleEventCalendarDialogClose}
        handleEventDetailClick={this.handleEventDetailClick(this.state.selectedEvent.id)}/> : null}
     
      </div>
    );
  }
}

export default EventCalendar;
