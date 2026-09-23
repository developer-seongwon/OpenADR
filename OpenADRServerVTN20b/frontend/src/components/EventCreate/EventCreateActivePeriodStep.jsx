import React from 'react';











import Grid from '@mui/material/Grid';







import Divider from '@mui/material/Divider';












import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';




import {TimezonePicker, DateAndTimePicker, DurationPicker} from '../common/TimePicker'


export class EventCreateEventActivePeriodStep extends React.Component {

  constructor( props ) {
    super( props );
    this.state = {};
    this.state.needAdvancedActivePeriod = false;
    if (props.activePeriod.notificationDuration !== ""
      ||props.activePeriod.rampUpDuration !== ""
      || props.activePeriod.toleranceDuration !== ""
      || props.activePeriod.recoveryDuration !== "") {
      this.state.needAdvancedActivePeriod = true;
    }

   
  }

  // 예전 componentWillReceiveProps 를 옮겼다. 부모가 다시 그릴 때(props 객체가 바뀔 때)만 보고,
  // 고급 항목 중 하나라도 값이 있으면 고급 영역을 펼친다. 이미 펼쳐져 있으면 다시 setState 하지 않는다.
  // 원래 코드의 rampUpDuration 줄 앞에 NBSP(U+00A0) 가 섞여 있었는데 같이 정리했다
  componentDidUpdate(prevProps) {
    if (prevProps !== this.props
      && !this.state.needAdvancedActivePeriod
      && (this.props.activePeriod.notificationDuration !== ""
        || this.props.activePeriod.rampUpDuration !== ""
        || this.props.activePeriod.toleranceDuration !== ""
        || this.props.activePeriod.recoveryDuration !== "")) {
      this.setState({needAdvancedActivePeriod: true});
    }
  }

   handleTimezoneChange = (timezone) => {
    let signal = this.props.activePeriod;
    signal.timezone = timezone;
    this.props.onChange(signal);
  }

  handleStartChange = (start) => {
    let signal = this.props.activePeriod;
    signal.start = start;
    this.props.onChange(signal);
  }

   handleDurationChange = (duration) => {
    let signal = this.props.activePeriod;
    signal.duration = duration;
    this.props.onChange(signal);
  }


  handleNeedAdvancedActivePeriod = (e) => {
    if(!e.target.checked) {
      let activePeriod = this.props.activePeriod;
      activePeriod.notificationDuration = "";
      activePeriod.recoveryDuration = "";
      activePeriod.toleranceDuration = "";
      activePeriod.rampUpDuration = "";
      this.props.onChange(activePeriod);
    }
    this.setState({needAdvancedActivePeriod: e.target.checked});
  }

  handleNotificationDurationChange = (duration) => {
    let activePeriod = this.props.activePeriod;
    activePeriod.notificationDuration = duration;
    this.props.onChange(activePeriod);
  }

  handleRecoveryDurationChange = (duration) => {
    let activePeriod = this.props.activePeriod;
    activePeriod.recoveryDuration = duration;
    this.props.onChange(activePeriod);
  }

  handleToleranceDurationChange = (duration) => {
    let activePeriod = this.props.activePeriod;
    activePeriod.toleranceDuration = duration;
    this.props.onChange(activePeriod);
  }

  handleRampUpDurationChange = (duration) => {
    let activePeriod = this.props.activePeriod;
    activePeriod.rampUpDuration = duration;
    this.props.onChange(activePeriod);
  }

  


  render() {
    const {classes, hasError, activePeriod} = this.props;

    



    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>

        <Grid container spacing={ 3 } >
          <Grid size={2} />
          <Grid size={2}>
            <TimezonePicker label="Timezone"  classes={classes} value={activePeriod.timezone} onChange={this.handleTimezoneChange}/>
          </Grid> 
          <Grid size={3}>
             <DateAndTimePicker classes={ classes } field="Start Date" error={hasError && activePeriod.start == null}
               value={activePeriod.start} onChange={this.handleStartChange} />
          </Grid>
          <Grid size={2}>
             <DurationPicker classes={ classes } field="Duration (minutes)"  error={hasError && activePeriod.duration === ""}
                 value={activePeriod.duration} onChange={this.handleDurationChange}/>
          </Grid>
          <Grid size={1}>
             <FormControlLabel
            control={
              <Checkbox
                checked={this.state.needAdvancedActivePeriod}
                onChange={this.handleNeedAdvancedActivePeriod}
                value="Intervals"
                color="primary"
              />
            }
            label="Advanced Period"
          />
          </Grid>
          <Grid size={2} />
        </Grid>

        {(this.state.needAdvancedActivePeriod) ? <Grid container
                style={ { marginTop: 20 } }
                spacing={ 3 }>
            <Grid size={2} />
            <Grid size={8}>
              <Divider />
            </Grid>
            <Grid size={2} />
          </Grid>: null}

        {(this.state.needAdvancedActivePeriod) ? <Grid container 
            style={ { marginTop: 20 } }
            spacing={ 3 } >
          <Grid size={2} />
          <Grid size={2}>
            <DurationPicker classes={ classes } field="Notification Duration (minutes)" 
                 value={activePeriod.notificationDuration} onChange={this.handleNotificationDurationChange}/>
          </Grid> 
          <Grid size={2}>
             <DurationPicker classes={ classes } field="RampUp Duration (minutes)" 
                 value={activePeriod.rampUpDuration} onChange={this.handleRampUpDurationChange}/>
          </Grid>
           <Grid size={2}>
             <DurationPicker classes={ classes } field="Tolerance Duration (minutes)" 
                 value={activePeriod.toleranceDuration} onChange={this.handleToleranceDurationchange}/>
          </Grid>
          <Grid size={2}>
             <DurationPicker classes={ classes } field="Recovery Duration (minutes)" 
                 value={activePeriod.recoveryDuration} onChange={this.handleRecoveryDurationChange}/>
          </Grid>
          
          <Grid size={2} />
        </Grid> : null}

      </Grid>
    );
  }
}

export default EventCreateEventActivePeriodStep;
