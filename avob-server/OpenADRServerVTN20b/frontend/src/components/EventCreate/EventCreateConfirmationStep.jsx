import React from 'react';
import { t } from '../../i18n';







import TextField from '@mui/material/TextField';



import Grid from '@mui/material/Grid';







import Divider from '@mui/material/Divider';

import { signalTypeLabel } from '../common/EventSignalPanel';
import { targetTypeLabel } from '../common/EventTargetPanel';
import { timestampToZoned } from '../../utils/time';

// 확인 단계는 읽기 전용 칸만 쓴다
var ReadOnlyField = (props) => (
  <TextField label={ props.label }
             value={ (props.value != null) ? props.value : "" }
             className={ props.className }
             margin="dense"
             variant="outlined"
             fullWidth={ true }
             slotProps={{
               input: { readOnly: true }
             }} />
);

// 분 입력값을 그대로 보여 준다(칸 이름에 분이 붙어 있다). 알림, 허용 오차는 비우면 0 으로 보낸다(EventCreate.handleCreateEvent)
var minutesText = (minutes, zeroWhenEmpty) => {
  if (minutes == null || minutes === "") {
    return zeroWhenEmpty ? "0" : "-";
  }
  return String(minutes);
}

export class EventCreateConfirmationStep extends React.Component {

  render() {
    const {classes, descriptor, eventSignal, activePeriod, eventTarget} = this.props;
    // 활성 기간은 고른 시간대로 보여 준다(활성 기간 단계와 같다)
    var start = (activePeriod && activePeriod.start != null) ? timestampToZoned(activePeriod.start, activePeriod.timezone) : null;
    var targets = eventTarget || [];

    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>
        <Grid container spacing={ 3 }>
            <Grid size={2} />
            <Grid size={4}>
              <TextField label={ t( 'event.marketContext' ) }
                         value={ descriptor.marketContext }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={1}>
              <TextField label={ t( 'event.priority' ) }
                         value={ descriptor.priority }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={1}>
              <TextField label={ t( 'event.responseRequired' ) }
                         value={ descriptor.responseRequired }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={2}>
              <TextField label={ t( 'event.scope' ) }
                         value={ (descriptor.testEvent ) ? t( 'event.testEvent' ) : t( 'event.productionEvent' ) }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={2} />
          </Grid>

        {/* 예전에는 활성 기간을 확인 단계에서 볼 수 없었다 */}
        { activePeriod ? <Grid container spacing={ 3 }>
            <Grid size={2} />
            <Grid size={3}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.startDate' ) }
                value={ start ? start.date + " " + start.time + " " + activePeriod.timezone : "-" } />
            </Grid>
            <Grid size={1}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.durationMinutes' ) }
                value={ minutesText( activePeriod.duration, false ) } />
            </Grid>
            <Grid size={1}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.notificationMinutes' ) }
                value={ minutesText( activePeriod.notificationDuration, true ) } />
            </Grid>
            <Grid size={1}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.toleranceMinutes' ) }
                value={ minutesText( activePeriod.toleranceDuration, true ) } />
            </Grid>
            <Grid size={1}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.rampUpMinutes' ) }
                value={ minutesText( activePeriod.rampUpDuration, false ) } />
            </Grid>
            <Grid size={1}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.recoveryMinutes' ) }
                value={ minutesText( activePeriod.recoveryDuration, false ) } />
            </Grid>
            <Grid size={2} />
          </Grid> : null }

        <Grid container
            style={ { marginTop: 20, marginBottom:10 } }
            spacing={ 3 }>
        <Grid size={2} />
        <Grid size={8}>
          <Divider />
        </Grid>
        <Grid size={2} />
      </Grid>

        {eventSignal.map((signal, index) => {
            return (
              <Grid container spacing={ 3 }  key={"signal_confirmation_panel_"+index}>
                <Grid size={2}>
                  {/* 비워 두면 VTN 이 순번을 쓴다 */}
                  <TextField label={ t( 'event.signalId' ) }
                             value={ signal.signalId ? signal.signalId : "(" + index + ")" }
                             className={ classes.textField }
                             margin="dense"
                             variant="outlined"
                             fullWidth={ true }
                             slotProps={{
                               input: { readOnly: true }
                             }} />
                </Grid>
                <Grid size={2}>
                  <TextField label={ t( 'event.signalName' ) }
                             value={ signal.signalName }
                             className={ classes.textField }
                             margin="dense"
                             variant="outlined"
                             fullWidth={ true }
                             slotProps={{
                               input: { readOnly: true }
                             }} />
                </Grid>
                <Grid size={2}>
                  <TextField label={ t( 'event.signalType' ) }
                             value={ signalTypeLabel(signal.signalType) }
                             className={ classes.textField }
                             margin="dense"
                             variant="outlined"
                             fullWidth={ true }
                             slotProps={{
                               input: { readOnly: true }
                             }} />
                </Grid>
                <Grid size={2}>
                  <TextField label={ t( 'event.unit' ) }
                             value={ signal.unitType }
                             className={ classes.textField }
                             margin="dense"
                             variant="outlined"
                             fullWidth={ true }
                             slotProps={{
                               input: { readOnly: true }
                             }} />
                </Grid>
                <Grid size={1}>
                  <TextField label={ t( 'event.currentValue' ) }
                             value={ signal.currentValue }
                             className={ classes.textField }
                             margin="dense"
                             variant="outlined"
                             fullWidth={ true }
                             slotProps={{
                               input: { readOnly: true }
                             }} />
                </Grid>
                <Grid size={1}>
                  <TextField label={ t( 'event.nbIntervals' ) }
                             value={ signal.intervals.length}
                             className={ classes.textField }
                             margin="dense"
                             variant="outlined"
                             fullWidth={ true }
                             slotProps={{
                               input: { readOnly: true }
                             }} />
                </Grid>
                <Grid size={2} />
              </Grid>
            );
          })}

        <Grid container
           style={ { marginTop: 20, marginBottom:10 } }
           spacing={ 3 }>
       <Grid size={2} />
       <Grid size={8}>
         <Divider />
       </Grid>
       <Grid size={2} />
     </Grid>
        {/* 예전에는 대상 수 자리에 12 가 박혀 있었다. 대상 단계에서 넣은 대상을 하나씩 보여 준다 */}
        <Grid container spacing={ 3 }>
            <Grid size={2} />
            <Grid size={2}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.nbTargets' ) } value={ targets.length } />
            </Grid>
            <Grid size={6} />
            <Grid size={2} />
          </Grid>
        { targets.map((target, index) => (
          <Grid container spacing={ 3 } key={ "target_confirmation_" + index }>
            <Grid size={2} />
            <Grid size={2}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.targetType' ) } value={ targetTypeLabel(target.targetType) } />
            </Grid>
            <Grid size={6}>
              <ReadOnlyField className={ classes.textField } label={ t( 'event.targetId' ) } value={ target.targetId } />
            </Grid>
            <Grid size={2} />
          </Grid>
        )) }
      </Grid>
    );
  }
}

export default EventCreateConfirmationStep;
