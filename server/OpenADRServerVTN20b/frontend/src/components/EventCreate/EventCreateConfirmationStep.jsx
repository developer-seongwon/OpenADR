import React from 'react';







import TextField from '@mui/material/TextField';



import Grid from '@mui/material/Grid';







import Divider from '@mui/material/Divider';

import { signalTypeLabel } from '../common/EventSignalPanel';


export class EventCreateConfirmationStep extends React.Component {

  render() {
    const {classes, descriptor, eventSignal} = this.props;

    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>
        <Grid container spacing={ 3 }>
            <Grid size={2} />
            <Grid size={4}>
              <TextField label="Market Context"
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
              <TextField label="Priority"
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
              <TextField label="Response required"
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
              <TextField label="Scope"
                         value={ (descriptor.testEvent ) ? "Test Event": "Production Event"}
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
                  <TextField label="Signal ID"
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
                  <TextField label="Signal Name"
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
                  <TextField label="Signal Type"
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
                  <TextField label="Unit"
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
                  <TextField label="Current Value"
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
                  <TextField label="Nb Intervals"
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
        <Grid container spacing={ 3 }>
            <Grid size={2} />
            <Grid size={2}>
              <TextField label="Nb Targeted devices"
                         value={ 12 }
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
      </Grid>
    );
  }
}

export default EventCreateConfirmationStep;
