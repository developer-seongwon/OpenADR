import React from 'react';
import TextField from '@mui/material/TextField';

import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';

import {browserTimezone, timestampToZoned, zonedToTimestamp} from '../../utils/time'
import timezone from './timezone'

// 날짜는 props.timezone(없으면 브라우저 시간대)의 자정이다.
// 예전에는 보여 줄 때는 브라우저 시간대, 고른 값은 UTC 로 읽었다
export var DatePicker = (props) => {
  const { classes } = props;
  var tz = props.timezone || browserTimezone();
  var valDate = timestampToZoned(props.value != null ? props.value : Date.now(), tz).date;

  var onDateChange = (e) => {
    props.onChange(zonedToTimestamp(e.target.value, "00:00", tz));
  }


  return (
    <TextField
      required
      error={props.error}
      label={props.field}
      type="date"
      className={classes.textField}
      value={valDate}
      onChange={onDateChange}
      fullWidth={props.fullWidth != null && props.fullWidth }
      slotProps={{
        input: {style:{marginTop:24}},

        inputLabel: {
          shrink: true,
        }
      }} />
  );
}

// 보이는 날짜, 시각은 props.timezone(없으면 브라우저 시간대) 기준이다.
// 예전에는 보여 줄 때는 브라우저 시간대, 고친 값은 UTC 로 읽어서 한국에서 시각을 고치면 9시간 뒤로 갔다.
// 날짜나 시각을 지우면 null 을 넘긴다
export var DateAndTimePicker = (props) => {
  const { classes } = props;
  var tz = props.timezone || browserTimezone();
  var shown = timestampToZoned(props.value != null ? props.value : Date.now(), tz);
  var valDate = shown.date;
  var valTime = (props.value != null) ? shown.time : "00:00";

  var onDateChange = (e) => {
    props.onChange(zonedToTimestamp(e.target.value, valTime, tz));
  }

  var onTimeChange = (e) => {
    props.onChange(e.target.value ? zonedToTimestamp(valDate, e.target.value, tz) : null);
  }

  return (
    <span>
      <TextField required error={props.error}
        label={props.field}
        type="date"
        className={classes.textField}
        value={valDate}
        onChange={onDateChange}
        slotProps={{
          inputLabel: {
            shrink: true,
          }
        }}
      />
      <TextField
        required
        error={props.error}
        label={props.field}
        type="time"
        className={classes.textField}
        value={valTime}
        helperText={ tz }
        onChange={ onTimeChange}
        slotProps={{
          htmlInput: {
            step: 60, // 5 min
          },

          inputLabel: {
            shrink: true,
          }
        }} />
    </span>
  );
}

export var DurationPicker = (props) => {
  const { classes } = props;
   var val = "";
  if(props.value != null) {
    val = props.value;
  }

  return (
    <TextField required error={props.error}
      label={props.field}
      type="number"
      className={classes.textField}
      fullWidth={true}
      value={val}
      onChange={(e) => {
        props.onChange(e.target.value);
      }}
      slotProps={{
        inputLabel: {
          shrink: true,
        }
      }}
    />
  );
}

const labelStyle = {

  boxSizing: 'border-box',
  color: 'rgba(0, 0, 0, 0.54)',
  fontSize: '1rem',
  fontWeight: 400,

  lineHeight: 1,
  transition: 'color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms',
  transform: 'translate(0, 1.5px) scale(0.75)',
  transformOrigin: 'top left',
  top: 0,
  left: 0,
}


export var TimezonePicker = (props) => {
   var val = "";
  if(props.value != null) {
    val = props.value;
  }

  var timezoneView = []

    for (var i in timezone) {
      var tz = timezone[i];
      var offset = Math.abs(tz.offset);
      var isNegative = tz.offset < 0;
      var offsetMinute = offset * 60;
      var remainder = offsetMinute % 60;
      var hours = Math.floor(offsetMinute / 60);
      var offsetStr = "GMT";
      if(isNegative){
        offsetStr += "-";
      }
      else {
        offsetStr += "+";
      }
      if(hours < 10) {
        offsetStr += "0"
      }
      offsetStr += hours + ""
      offsetStr += ":"
      if(remainder < 10) {
        offsetStr += "0"
      }
      offsetStr += remainder;
      timezoneView.push( <MenuItem key={ tz.name } value={ tz.name }>
                                   { tz.name } ({offsetStr})
                                   </MenuItem> )
    }

    // 목록에 없는 값(브라우저 시간대가 목록에 없을 때 등)도 고른 값으로 보이게 맨 앞에 넣는다
    if (val !== "" && !timezone.some((item) => item.name === val)) {
      timezoneView.unshift( <MenuItem key={ val } value={ val }>{ val }</MenuItem> )
    }


  return (
       <FormControl style={{marginLeft:8}}>
          <FormLabel style={ labelStyle } component="label">
            {props.label}
          </FormLabel>
          <Select value={ val}
                        style={ { marginTop: 0 } }
                         onChange={(e) => {
                          props.onChange(e.target.value);
                        }}
                        
                        inputProps={ { name: 'timezone', id: 'timezone_select',  } }
                        >
                  { timezoneView }
          </Select>
        </FormControl>
  );
}