import React from 'react';



import EventDetailHeader from './EventDetailHeader'
import Divider from '@mui/material/Divider';
import TextField from '@mui/material/TextField';

import Grid from '@mui/material/Grid';

import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';

import {formatTimestamp} from '../../utils/time'

var EventTextField = (props) => {
  var value = (props.value != null) ? props.value : "";
  return (
    <TextField label={ props.field }
               value={ value }
               className={ props.className }
               margin="normal"
               fullWidth={true}
               slotProps={{
                 input: { readOnly: true, }
               }} />
  );
}

export class EventDetailActivePeriod extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}


  }

  render() {
    const {classes, event} = this.props;
        var startDatetime = formatTimestamp(event.activePeriod.start);
    var hasAvancedActivePeriod = false;
    if(event.activePeriod.notificationDuration != null
      || event.activePeriod.rampUpDuration != null
      || event.activePeriod.recoveryDuration != null
      || event.activePeriod.toleranceDuration != null) {
      hasAvancedActivePeriod = true;
    }
    return (
      <div className={ classes.root } >
        <EventDetailHeader classes={classes} event={event}/>
        <Divider style={ { marginTop: '20px' } } />

        <Grid container spacing={ 3 }>
           <Grid size={3}>
             <EventTextField className={ classes.textField } field="Start Datetime" 
             value={ startDatetime.date + " " +startDatetime.time + " " + startDatetime.tz } />
           </Grid>
           <Grid size={3}>
             <EventTextField className={ classes.textField } field="Duration" value={ event.activePeriod.duration } />
           </Grid>
            <Grid size={2}>
              <FormControlLabel
             control={
               <Checkbox
                 checked={hasAvancedActivePeriod}
                 value="advancedActivePeriod"
                 color="primary"
               />
             }
             label="Advanced Active Period"
           />
         </Grid>
         </Grid>

        {(hasAvancedActivePeriod) ?  <Grid container spacing={ 3 }>
          <Grid size={3}>
            <EventTextField className={ classes.textField } field="Notification Duration" value={ event.activePeriod.notificationDuration } />
          </Grid>
          <Grid size={3}>
            <EventTextField className={ classes.textField } field="Ramp Up Duration" value={ event.activePeriod.rampUpDuration } />
          </Grid>
          <Grid size={3}>
            <EventTextField className={ classes.textField } field="Recovery Duration" value={ event.activePeriod.recoveryDuration } />
          </Grid>
          <Grid size={3}>
            <EventTextField className={ classes.textField } field="Tolerance Duration" value={ event.activePeriod.toleranceDuration } />
          </Grid>
        </Grid> : null}

        <Divider style={ { marginTop: '20px' } } />

      </div>
    );
  }
}

export default EventDetailActivePeriod;
