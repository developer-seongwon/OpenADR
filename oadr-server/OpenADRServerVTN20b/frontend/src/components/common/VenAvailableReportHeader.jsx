import React from 'react';
import { t } from '../../i18n';


import TextField from '@mui/material/TextField';
import Grid from '@mui/material/Grid';

import { isHistoryReport, isTelemetryReport} from '../../utils/venReport'
import {DateAndTimePicker, DurationPicker } from './TimePicker'







var VenReportTextField = (props) => {
  var value = (props.value != null) ? props.value : "";
  return (
    <TextField label={ props.field }
               value={ value }
               className={ props.className }
               margin="normal"
               fullWidth={true}
               slotProps={{
                 input: { shrink: "true", readOnly: true, }
               }} />
  );
}



export function VenAvailableReportHeader (props) {
  const { classes, availableReport} = props;
  var startView = null;
  var durationView = null;
  if(isHistoryReport(availableReport)){
    if(availableReport.start) {
     
      startView = <Grid size={3}>
        <VenReportTextField className={ classes.textField } field={ t( 'report.startDate' ) } value={ availableReport.start } />
      </Grid>
    }
    if(availableReport.duration) {
      durationView = <Grid size={3}>
        <VenReportTextField className={ classes.textField } field={ t( 'report.duration' ) } value={ availableReport.duration } />
      </Grid>
    }
  }
    
  return (
    <div>
      <Grid>
        <Grid container spacing={ 3 }>
          <Grid size={3}>
            <VenReportTextField className={ classes.textField } field={ t( 'report.eiReportId' ) } value={ availableReport.reportId } />
          </Grid>
          <Grid size={3}>
            <VenReportTextField className={ classes.textField } field={ t( 'report.reportName' ) } value={ availableReport.reportName } />
          </Grid>
          <Grid size={3}>
            <VenReportTextField className={ classes.textField } field={ t( 'report.reportSpecifierId' ) } value={ availableReport.reportSpecifierId } />
          </Grid>
          <Grid size={3}>
            <VenReportTextField className={ classes.textField } field={ t( 'report.createdDatetime' ) } value={ availableReport.createdDatetime } />
          </Grid>
        </Grid>

        {isHistoryReport(availableReport) ? ( <Grid container spacing={ 3 }>
          {startView} 
          {durationView}
        </Grid>) : null}

      </Grid>
    </div>
  );
}

export function VenAvailableReportParamsHeader (props) {
  const { classes, availableReport} = props;
  var startView = null;
  var durationView = null;
  var granularityView = null;
  var reportBackDurationView = null;
  if(isHistoryReport(availableReport)){
    var startError = (props.hasError && props.start == null);
    var durationError = (props.hasError && props.duration == null);
    startView = <Grid size={3}>
        <DateAndTimePicker error={startError} classes={ classes } field={ t( 'report.startDate' ) }
        value={props.start} onChange={props.onStartChange} />
      </Grid>
    durationView = <Grid size={3}>
        <DurationPicker error={durationError} classes={ classes } field={ t( 'report.durationMinutes' ) }
        value={props.duration} onChange={props.onDurationChange}/>
      </Grid>
  }

  if(isTelemetryReport(availableReport)){

    var granularityError = (props.hasError && props.granularity == null);
    var reportBackDurationError = (props.hasError && props.reportBackDuration == null);

    granularityView = <Grid size={3}>
        <DurationPicker error={granularityError} classes={ classes } field={ t( 'report.granularityMinutes' ) }
        value={props.granularity} onChange={props.onGranularityChange}/>
      </Grid>
    reportBackDurationView = <Grid size={3}>
        <DurationPicker error={reportBackDurationError} classes={ classes } field={ t( 'report.reportBackMinutes' ) }
        value={props.reportBackDuration} onChange={props.onReportBackDurationChange}/>
      </Grid>
  }
    
  return (
    <div>
      <Grid>
        {isTelemetryReport(availableReport) ? ( <Grid container spacing={ 3 }>
          {granularityView} 
          {reportBackDurationView}
        </Grid>) : null}

        { isHistoryReport(availableReport) ? ( <Grid container spacing={ 3 }>
          {startView} 
          {durationView}
        </Grid>) : null}

      </Grid>
    </div>
  );
}